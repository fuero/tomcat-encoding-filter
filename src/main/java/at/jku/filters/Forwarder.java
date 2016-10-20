package at.jku.filters;

public interface Forwarder<T, S> {
    T to(S target);
}
