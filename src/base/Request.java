package base;

import enums.TypeWorkshop;

public class Request {

    private Client client;
    private TypeWorkshop type;

    public Request(Client client, TypeWorkshop type) {
        this.client = client;
        this.type = type;
    }

    public Client getClient() {
        return client;
    }

    public TypeWorkshop getType() {
        return type;
    }
}

