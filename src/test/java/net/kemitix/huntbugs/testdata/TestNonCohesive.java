package net.kemitix.huntbugs.testdata;

class TestNonCohesive {

    private String left;

    private String right;

    private int counter = 0;

    private String format = "Hello, %s!";

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public int counter() {
        return counter;
    }

    public void increment() {
        counter += 1;
    }

    public String getFullFormat(final String ignored) {
        final String fullFormat = left + format + right;
        return fullFormat;
    }

    public String sayHello(final TestCohesive name) {
        return String.format(getFullFormat("ignore me"), name);
    }

    public Boolean isCounter() {
        return counter > 0;
    }

    public void setCounter(final int value) {
        counter = value;
    }

}
