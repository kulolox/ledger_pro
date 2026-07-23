const BOOK_KEY = "currentBookId";

export const getCurrentBookId = (): number => {
  return uni.getStorageSync(BOOK_KEY);
};

export const setCurrentBookId = (bookId: number): void => {
  uni.setStorageSync(BOOK_KEY, bookId);
};
