package base;

import enums.TypeReq;

public class Request {

    private Client client;
    private TypeReq typeReq;

    public Request(Client client, TypeReq typeReq) {
        this.client = client;
        this.typeReq = typeReq;
    }

    public Client getClient() {
        return client;
    }

    public TypeReq getTypeReq() {
        return typeReq;
    }
}

