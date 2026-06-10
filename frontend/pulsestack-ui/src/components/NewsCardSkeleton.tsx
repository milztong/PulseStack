/**
 * Skeleton-Loader für NewsCard — verhindert Layout-Shift beim Laden.
 * Wird angezeigt solange Daten aus der API oder dem WebSocket noch nicht da sind.
 */
export function NewsCardSkeleton() {
  return (
    <div className="bg-gray-900 rounded-xl p-4 border border-gray-800 animate-pulse">
      <div className="flex items-center gap-2 mb-3">
        {/* Source Badge Skeleton */}
        <div className="w-16 h-5 rounded-full bg-gray-700" />
        {/* Timestamp Skeleton */}
        <div className="w-24 h-4 rounded bg-gray-800 ml-auto" />
      </div>
      {/* Title Skeleton — zwei Zeilen */}
      <div className="space-y-2 mb-3">
        <div className="h-4 rounded bg-gray-700 w-full" />
        <div className="h-4 rounded bg-gray-700 w-4/5" />
      </div>
      {/* Author Skeleton */}
      <div className="h-3 rounded bg-gray-800 w-1/3" />
    </div>
  );
}

/** Rendert n Skeleton-Karten — für initiales Laden des Feeds */
export function NewsCardSkeletonList({ count = 5 }: { count?: number }) {
  return (
    <div className="flex flex-col gap-3">
      {Array.from({ length: count }).map((_, i) => (
        <NewsCardSkeleton key={i} />
      ))}
    </div>
  );
}
