package com.projekttellus.ui;

/** A disposable visual projection. Progress is normalized; no clocks, database or domain writes. */
interface PlanetEffect {
    void frame(double progress);
    void dispose();
}
