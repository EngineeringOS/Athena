<!--
  Adapted from Graphite PanelSubdivision.svelte:1-199,254-272 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  let { splitId, beforeIndex, axis, dispatch } = $props();

  let active = false;
  let pointerId;
  let lastCoordinate = 0;
  let gutter;

  function coordinate(event) {
    return axis === "Horizontal" ? event.clientX : event.clientY;
  }

  function beginResize(event) {
    if (event.button !== 0 || active) return;
    const parent = event.currentTarget.parentElement;
    const bounds = parent.getBoundingClientRect();
    const available = Math.round(axis === "Horizontal" ? bounds.width : bounds.height);
    dispatch("BeginResize", {
      split_id: splitId,
      before_index: beforeIndex,
      available_px: available,
    });
    active = true;
    pointerId = event.pointerId;
    lastCoordinate = coordinate(event);
    gutter = event.currentTarget;
    gutter.setPointerCapture(pointerId);
    window.addEventListener("pointermove", resize);
    window.addEventListener("pointerup", commitResize);
    window.addEventListener("pointercancel", abortResize);
    window.addEventListener("keydown", keydown);
    window.addEventListener("mousedown", mouseAbort);
  }

  function resize(event) {
    if (!active) return;
    const current = coordinate(event);
    const delta = Math.round(current - lastCoordinate);
    if (delta !== 0) {
      dispatch("ResizeAdjacent", {
        split_id: splitId,
        before_index: beforeIndex,
        delta_px: delta,
      });
      lastCoordinate = current;
    }
  }

  function commitResize() {
    if (!active) return;
    dispatch("CommitResize");
    cleanup();
  }

  function abortResize() {
    if (!active) return;
    dispatch("AbortResize");
    cleanup();
  }

  function resetAdjacent(event) {
    event.preventDefault();
    dispatch("ResetAdjacent", { split_id: splitId, before_index: beforeIndex });
  }

  function keydown(event) {
    if (event.key === "Escape") abortResize();
  }

  function mouseAbort(event) {
    if (event.button === 2) abortResize();
  }

  function cleanup() {
    if (gutter?.hasPointerCapture(pointerId)) gutter.releasePointerCapture(pointerId);
    active = false;
    pointerId = undefined;
    gutter = undefined;
    window.removeEventListener("pointermove", resize);
    window.removeEventListener("pointerup", commitResize);
    window.removeEventListener("pointercancel", abortResize);
    window.removeEventListener("keydown", keydown);
    window.removeEventListener("mousedown", mouseAbort);
  }
</script>

<div
  class:horizontal={axis === "Horizontal"}
  class:vertical={axis === "Vertical"}
  class="workspace-gutter"
  data-gutter-axis={axis}
  role="separator"
  aria-orientation={axis === "Horizontal" ? "vertical" : "horizontal"}
  onpointerdown={beginResize}
  ondblclick={resetAdjacent}
></div>
