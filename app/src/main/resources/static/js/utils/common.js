
// /utils/common.js
export function debounce(fn, delay = 250) {
    let t = null;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...args), delay);
    };
  }
  
  export function formatDateTime(iso) {
    if (!iso) return '-';
    const d = new Date(iso);
    return `${d.toLocaleDateString()} ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  export function inTimeWindow(start, end, windowName) {
    // start/end expected as "HH:mm:ss" or "HH:mm"
    const [sH] = (start || '00:00').split(':').map(Number);
    const [eH] = (end || '23:59').split(':').map(Number);
    const windowRanges = {
      morning: [6, 12],
      afternoon: [12, 17],
      evening: [17, 22]
    };
    const [minH, maxH] = windowRanges[windowName] || [0, 24];  return (sH < maxH && eH > minH);
}