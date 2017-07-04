package pl.rychu.jew.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilterChain;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.logaccess.LogAccess;



public class ModNotifier implements Runnable {
	private final Logger log = LoggerFactory.getLogger(ModNotifier.class);

	private final LogAccess logAccess;
	private int logAccessVersion;

	private long prevMaxIndexF;
	private long prevMinIndexB;
	private long prevMaxIndexLabel;

	private final ModelFacade modelFacade;

	private final LogLineFilterChain filterChain;

	// -------

	ModNotifier(LogAccess logAccess, int logAccessVersion
	 , long startIndex, LogLineFilterChain filterChain, ModelFacade modelFacade) {
		this.logAccess = logAccess;
		this.logAccessVersion = logAccessVersion;
		this.prevMaxIndexF = startIndex;
		this.prevMinIndexB = startIndex;
		this.filterChain = filterChain;
		this.modelFacade = modelFacade;
	}

	// -------

	@Override
	public void run() {
		log.debug("running");
		while (!Thread.interrupted()) {
			try {
				process();
			} catch (RuntimeException e) {
				log.error("error during processing", e);
			} catch (BadVersionException e) {
				log.debug("bad version");
				// ignore; version reset will be handled in next cycle
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				break;
			}
		}

		log.debug("quitting gracefully");
	}

	private void process() throws BadVersionException {
		final int version = logAccess.getVersion();
		if (version != logAccessVersion) {
			log.debug("schedule reset; {} != {}", version, logAccessVersion);
			modelFacade.clear(version);
			prevMaxIndexF = 0;
			prevMinIndexB = 0;
			logAccessVersion = version;
			return;
		}

		final int maxSlice = 10000;
		while (true) {
			final long maxIndexF = logAccess.size(version);
			final long minIndexB = 0;

			if (maxIndexF==prevMaxIndexF && minIndexB==prevMinIndexB && maxIndexF==prevMaxIndexLabel) {
				break;
			}

			if (Thread.interrupted()) {
				Thread.currentThread().interrupt();
				break;
			}

			if (maxIndexF != prevMaxIndexLabel) {
				modelFacade.setSourceSize(maxIndexF);
				prevMaxIndexLabel = maxIndexF;
			}

			if (maxIndexF != prevMaxIndexF) {
				final long maxF = Math.min(maxIndexF, prevMaxIndexF+maxSlice);
				final int maxSize = (int)(maxF - prevMaxIndexF);
				final long[] slice = new long[maxSize];
				int indexSlice = 0;
				for (long index=prevMaxIndexF; index<maxF; index++) {
					boolean applies = filterChain.apply(logAccess.get(index, version))
					 && (!filterChain.needsFullLine()
					 || filterChain.apply(logAccess.getFull(index, version)));
					if (applies) {
						slice[indexSlice++] = index;
					}
				}
				if (indexSlice != 0) {
					modelFacade.addF(slice, indexSlice);
				}

				prevMaxIndexF += maxSize;
			}

			if (minIndexB != prevMinIndexB) {
				final long minB = Math.max(minIndexB, prevMinIndexB-maxSlice);
				final int maxSize = (int)(prevMinIndexB - minB);
				final long[] slice = new long[maxSize];
				int indexSlice = 0;
				for (long index=prevMinIndexB-1; index>=minB; index--) {
					boolean applies = filterChain.apply(logAccess.get(index, version))
					 && (!filterChain.needsFullLine() ||
					 filterChain.apply(logAccess.getFull(index, version)));
					if (applies) {
						slice[indexSlice++] = index;
					}
				}
				if (indexSlice != 0) {
					modelFacade.addB(slice, indexSlice);
				}
				prevMinIndexB -= maxSize;
			}
		}
	}

}
