package base;

import enums.Names;
import enums.Surnames;

public class Client extends Person {

    public Client() {
    }

    @Override
    public Names getName() {
        return super.getName();
    }

    @Override
    public Surnames getSurname() {
        return super.getSurname();
    }
}