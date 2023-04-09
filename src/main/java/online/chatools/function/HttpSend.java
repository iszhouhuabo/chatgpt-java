package online.chatools.function;

/**
 * @author louye
 */
@FunctionalInterface
public interface HttpSend<T> {
    void sendToClient(T t);
}
