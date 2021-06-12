import java.util.stream.Stream;

public class Playground {
    public void fun() {
        Stream.of(0x1B, 0x2B)
                .map(Integer::byteValue)
                .forEach(System.out::write);
    }
}
