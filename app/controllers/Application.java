package controllers;

import java.io.*;
import java.net.*;
import java.util.*;

import models.Download;
import net.htmlparser.jericho.*;

import org.apache.commons.lang.*;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.service.*;

import play.*;
import play.mvc.*;

/**
 * Application controller.
 * 
 * @author garbagetown
 * 
 */
public class Application extends Controller {

    static {
        String hostKey = "http.proxyHost";
        String portKey = "http.proxyPort";
        String host = Play.configuration.getProperty(hostKey);
        String port = Play.configuration.getProperty(portKey);
        if (StringUtils.isNotEmpty(host) && StringUtils.isNotEmpty(port)) {
            System.setProperty(hostKey, host);
            System.setProperty(portKey, port);
        }
    }

    /**
     * index action.
     */
    public static void index() {
        render();
    }

    /**
     * documentation action.
     * 
     * @param version
     */
    public static void documentation() {
        redirect(String.format("/documentation/%s/home", Play.configuration.getProperty("version.latest")));
    }

    /**
     * download action.
     * 
     * @param action
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void download() throws MalformedURLException, IOException {

        Download latest = null;
        List<Download> upcomings = null;
        List<Download> olders = null;

        List<Element> tables = new Source(new URL("http://www.playframework.org/download"))
                .getAllElements(HTMLElementName.TABLE);
        // first table has latest version
        latest = toDownload(tables.get(0));
        // last table has older versions
        olders = toDownloads(tables.get(tables.size() - 1));
        // if there are more than two tables, middle of them might be upcomings
        if (tables.size() > 2) {
            upcomings = toDownloads(tables.get(1));
        }
        render(latest, upcomings, olders);
    }

    private static Download toDownload(Element table) {
        return toDownloads(table).get(0);
    }

    private static List<Download> toDownloads(Element table) {
        List<Download> downloads = new ArrayList<Download>();
        List<Element> elements = table.getAllElements(HTMLElementName.TR);
        for (Element element : elements) {
            List<Element> td = element.getAllElements(HTMLElementName.TD);
            String url = td.get(0).getChildElements().get(0).getAttributeValue("href");
            String date = td.get(1).getContent().toString().trim();
            String size = td.get(2).getContent().toString().trim();
            downloads.add(new Download(url, date, size));
        }
        return downloads;
    }

    /**
     * code action.
     * 
     * @param action
     */
    public static void code() {
        render();
    }

    /**
     * about action.
     * 
     * @throws IOException
     */
    public static void about() throws IOException {
        CollaboratorService service = new CollaboratorService();
        String owner = Play.configuration.getProperty("github.owner");
        String name = Play.configuration.getProperty("github.name");
        RepositoryId repository = new RepositoryId(owner, name);
        List<User> collaborators = new ArrayList<User>();
        UserService userService = new UserService();
        for (User user : service.getCollaborators(repository)) {
            collaborators.add(userService.getUser(user.getLogin()));
        }
        render(collaborators);
    }
}