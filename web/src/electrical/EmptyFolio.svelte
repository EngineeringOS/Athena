<div class="document-surface" data-document-surface>
  <article class="folio-page" data-empty-folio aria-label="Empty electrical folio">
    <div class="folio-grid" aria-hidden="true"></div>
    <div class="title-block-frame" data-title-block-frame aria-label="Empty title block frame">
      <span class="title-cell wide"></span>
      <span class="title-cell"></span>
      <span class="title-cell"></span>
      <span class="title-cell wide"></span>
      <span class="title-cell"></span>
      <span class="title-cell"></span>
    </div>
  </article>
</div>

<style>
  .document-surface {
    display: grid;
    min-width: 0;
    min-height: 0;
    padding: 28px 30px;
    overflow: auto;
    place-items: center;
    background: var(--color-4-dimgray);
  }

  .folio-page {
    position: relative;
    width: min(94%, 960px);
    min-width: 520px;
    aspect-ratio: 1.414 / 1;
    overflow: hidden;
    border: 1px solid #777;
    border-radius: 0;
    color: #222;
    background: #f8f8f6;
  }

  .folio-grid {
    position: absolute;
    inset: 4%;
    background-image:
      linear-gradient(#e1e1dd 1px, transparent 1px),
      linear-gradient(90deg, #e1e1dd 1px, transparent 1px);
    background-size: 16px 16px;
  }

  .title-block-frame {
    position: absolute;
    right: 2.5%;
    bottom: 2.5%;
    display: grid;
    grid-template: repeat(2, 1fr) / 2fr 1fr 1fr;
    width: 46%;
    height: 13%;
    border-top: 1px solid #333;
    border-left: 1px solid #333;
    background: #f8f8f6;
  }

  .title-cell {
    border-right: 1px solid #333;
    border-bottom: 1px solid #333;
  }

  .title-cell.wide {
    min-width: 0;
  }
</style>
