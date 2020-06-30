package cn.adonis.trader.framework;

public class BackTestException extends RuntimeException {

    private String msg;

    public BackTestException() {
        super();
    }

    public BackTestException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
