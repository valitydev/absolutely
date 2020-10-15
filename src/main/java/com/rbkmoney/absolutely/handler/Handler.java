package com.rbkmoney.absolutely.handler;

public interface Handler<T, I> {
    boolean accept(T change);
    void handle(I source, T change);
}
