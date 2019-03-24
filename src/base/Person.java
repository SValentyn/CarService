package base;

import enums.Names;
import enums.Surnames;

import java.util.Random;

abstract class Person {

    private Names name;
    private Surnames surname;

    Person() {
        Random random = new Random();
        this.name = Names.values()[random.nextInt(Names.values().length)];
        this.surname = Surnames.values()[random.nextInt(Surnames.values().length)];
    }

    public Names getName() {
        return name;
    }

    public Surnames getSurname() {
        return surname;
    }

}
