package org.docksidestage.app.web.fessup;

import javax.ws.rs.core.MediaType;

import org.docksidestage.app.web.base.FessupBaseAction;
import org.lastaflute.web.Execute;
import org.lastaflute.web.response.HtmlResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class FessupAction extends FessupBaseAction {

    private String account = "hoge";
    private String password = "hoge";

    @Execute
    public HtmlResponse index(Integer productId, FessupForm form) {
        // まだ、Formがない、returnがないのでコンパイルエラーです！
        // この後、Formを作って戻り値を指定しますので、ちょっとそのままで。
        //RestClient cli = new RestClient();
        String uri = "http://localhost:8080/json?q=python";

        //String xml = getString(uri, MediaType.APPLICATION_XML_TYPE);
        //System.out.println(xml);

        String json = getString(uri, MediaType.APPLICATION_JSON_TYPE);
        System.out.println("--------------------------------------------");
        System.out.println("json=" + json);

        return asHtml(path_Fessup_FessupHtml);
    }

    public void setRestClient(String account, String password) {
        this.account = account;
        this.password = password;
    }

    private Client getClient() {
        Client client = new Client();
        //client.addFilter(new HTTPBasicAuthFilter(account, password));
        return client;
    }

    public String getString(String url, MediaType type) {
        Client client = getClient();
        WebResource resource = client.resource(url);
        ClientResponse response = resource.accept(type).get(ClientResponse.class);
        switch (response.getStatus()) {
        case 200: // OK
            break;
        default:
            return String.format("Code:%s Entity:%s", response.getStatus(), response.getEntity(String.class));
        }
        return response.getEntity(String.class);
    }

}
