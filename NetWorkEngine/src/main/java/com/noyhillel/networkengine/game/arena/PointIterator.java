package com.noyhillel.networkengine.game.arena;

import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;

@Data
@RequiredArgsConstructor
public final class PointIterator implements Iterator<Point>, Iterable<Point> {
    private final List<Point> points;
    private final boolean loop;

    private int index = 0;

    public PointIterator(List<Point> points) {
        this(points, true);
    }
    @Override
    public boolean hasNext() {
        return loop || !(index >= points.size());
    }

    @Override
    public Point next() {
        if (index >= points.size() && loop) index = 0;
        Point point = points.get(index);
        index++;
        return point;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from immutable point list!");
    }

    public Point random() {
        return this.points.get(NetPlugin.getRandom().nextInt(points.size()));
    }

    @Override
    public Iterator<Point> iterator() {
        return points.listIterator();
    }
}