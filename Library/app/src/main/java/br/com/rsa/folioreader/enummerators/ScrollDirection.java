package br.com.rsa.folioreader.enummerators;

/**
 * Created by rodrigo.almeida on 14/04/15.
 */
public enum ScrollDirection {
    None(0),
    Right(1),
    Left(2),
    Up(3),
    Down(4);

    private int scrollDirection;

    ScrollDirection(int scrollDirection) {
        this.scrollDirection = scrollDirection;
    }
}
