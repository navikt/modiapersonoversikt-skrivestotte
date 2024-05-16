import { useEffect, useState } from "react";

export function tagsQuerySearch<T>(
  getTags: (t: T) => Array<string>,
  getText: (t: T) => Array<string>,
): (query: string, data: Array<T>, isSelected: (t: T) => boolean) => Array<T> {
  return (query, data, isSelected) => {
    if (query === "") {
      return data;
    }
    const text = query;
    const words = text
      .split(" ")
      .map((word) => word.toLowerCase().replace("#", ""))
      .filter((word) => word);

    return data.filter((element) => {
      const matchtext = getText(element).join("\u0000").toLowerCase();
      const searchTags = getTags(element).map((tag) => tag.toLowerCase());

      return (
        isSelected(element) ||
        words.every(
          (word) => matchtext.includes(word) || searchTags.includes(word),
        )
      );
    });
  };
}

export const usePreviousValue = <T>(value: T) => {
  const [prev, setPrev] = useState<T>();

  useEffect(() => {
    setPrev(value);
  }, [value]);

  return prev;
};
