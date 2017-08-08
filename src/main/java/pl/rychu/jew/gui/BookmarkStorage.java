package pl.rychu.jew.gui;

import java.util.*;

/**
 * Created on 08.08.2017.
 */
public class BookmarkStorage {
	private final Map<Integer, Long> bookmarks = new HashMap<>(20);
	private final Map<Integer, Long> bookmarkOffsets = new HashMap<>(20);
	private final Map<Long, List<Integer>> offsetToBookmark = new HashMap<>(20);
	private final Map<Long, List<Integer>> offsetToBookmarkPub = Collections.unmodifiableMap
	 (offsetToBookmark);

	public void put(int bkIndex, long rootLine, long fileOffset) {
		bookmarks.put(bkIndex, rootLine);
		bookmarkOffsets.put(bkIndex, fileOffset);
		recomputeReverse();
	}

	private void recomputeReverse() {
		offsetToBookmark.clear();
		for (Integer bkIndex : bookmarkOffsets.keySet()) {
			Long offset = bookmarkOffsets.get(bkIndex);
			List<Integer> list = offsetToBookmark.get(offset);
			if (list == null) {
				list = new ArrayList<>();
				offsetToBookmark.put(offset, list);
			}
			list.add(bkIndex);
		}
	}

	public Long get(int bkIndex) {
		return bookmarks.get(bkIndex);
	}

	public void remove(int bkIndex) {
		bookmarks.remove(bkIndex);
		bookmarkOffsets.remove(bkIndex);
		recomputeReverse();
	}

	public void clear() {
		bookmarks.clear();
		bookmarkOffsets.clear();
		recomputeReverse();
	}

	public BookmarkStorageView getView() {
		return new BookmarkStorageView() {
			@Override
			public Map<Long, List<Integer>> getOffsetToBookmark() {
				return offsetToBookmarkPub;
			}
		};
	}

	// =========

	public interface BookmarkStorageView {
		Map<Long, List<Integer>> getOffsetToBookmark();
	}

}
