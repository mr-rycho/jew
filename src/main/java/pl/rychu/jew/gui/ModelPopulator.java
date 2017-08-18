package pl.rychu.jew.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilterChain;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.logaccess.LogAccess;

import java.util.concurrent.atomic.AtomicReference;


public class ModelPopulator implements Runnable {
	private final Logger log = LoggerFactory.getLogger(ModelPopulator.class);

	private final LogAccess logAccess;
	private int logAccessVersion;

	private long prevMaxIndexF;
	private long prevMinIndexB;
	private long prevMaxIndexLabel;

	private final ModelFacade modelFacade;

	private LogLineFilterChain filterChain;

	private final AtomicReference<ModelPopulatorReconfig> modelPopulatorReconfig = new AtomicReference<>(null);

	// -------

	private ModelPopulator(LogAccess logAccess, ModelFacade modelFacade) {
		this.logAccess = logAccess;
		this.logAccessVersion = -1;
		this.prevMaxIndexF = 0;
		this.prevMinIndexB = 0;
		this.filterChain = new LogLineFilterChain();
		this.modelFacade = modelFacade;
	}

	public static ModelPopulator createAndStart(LogAccess logAccess, ModelFacade modelFacade) {
		ModelPopulator modelPopulator = new ModelPopulator(logAccess, modelFacade);

		new Thread(modelPopulator, "mod-notifier-thread").start();

		return modelPopulator;
	}

	// -------

	@Override
	public void run() {
		log.debug("running");
		while (true) {
			try {
				process();
			} catch (RuntimeException e) {
				log.error("error during processing", e);
			} catch (BadVersionException e) {
				log.debug("bad version");
				// ignore; version reset will be handled in next cycle
			}

			try {
				Thread.sleep(125);
			} catch (InterruptedException ignored) {}
		}
	}

	private void process() throws BadVersionException {
		if (modelPopulatorReconfig.get() != null) {
			ModelPopulatorReconfig reconfig = modelPopulatorReconfig.getAndSet(null);
			log.debug("schedule reconfig: {}", reconfig);
			modelFacade.clearSoft(logAccessVersion);
			boolean verEqual = logAccessVersion == reconfig.getSourceVersion();
			prevMaxIndexF = verEqual ? reconfig.getStartIndex() : 0;
			prevMinIndexB = verEqual ? reconfig.getStartIndex() : 0;
			filterChain = reconfig.getFilterChain();
			log.debug("populator reset {} / {}", prevMaxIndexF, filterChain);
		}

		int version = logAccess.getVersion();
		if (version != logAccessVersion) {
			log.debug("schedule reset; {} != {}", version, logAccessVersion);
			modelFacade.clearHard(version);
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

			if (modelPopulatorReconfig.get() != null) {
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

	public void reconfig(LogLineFilterChain filterChain, long startIndex,
	 int sourceVersion) {
		modelPopulatorReconfig.set(new ModelPopulatorReconfig(filterChain, startIndex, sourceVersion));
	}

	// ============

	public static class ModelPopulatorReconfig {
		private final LogLineFilterChain filterChain;
		private final long startIndex;
		private final int sourceVersion;

		ModelPopulatorReconfig(LogLineFilterChain filterChain, long startIndex, int sourceVersion) {
			this.filterChain = filterChain;
			this.startIndex = startIndex;
			this.sourceVersion = sourceVersion;
		}

		LogLineFilterChain getFilterChain() {
			return filterChain;
		}

		long getStartIndex() {
			return startIndex;
		}

		int getSourceVersion() {
			return sourceVersion;
		}

		@Override
		public String toString() {
			return "ModelPopulatorReconfig{ver=" + sourceVersion + "; startIndex=" + startIndex + "; " +
			 "filter=" + filterChain + "}";
		}
	}

}
