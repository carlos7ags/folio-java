package dev.foliopdf;

/**
 * A rectangular region to redact on a specific page.
 * Coordinates are in points, measured from the bottom-left of the page.
 *
 * @param page zero-based page index
 * @param x1   left coordinate
 * @param y1   bottom coordinate
 * @param x2   right coordinate
 * @param y2   top coordinate
 */
public record RedactRegion(int page, double x1, double y1, double x2, double y2) {}
