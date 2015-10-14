package com.tomtom.quickbuild.plugin.influxdb;

import javax.ws.rs.core.MediaType;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.quickbuild.Context;
import com.pmease.quickbuild.QuickbuildException;
import com.pmease.quickbuild.annotation.Editable;
import com.pmease.quickbuild.annotation.Scriptable;
import com.pmease.quickbuild.stepsupport.Step;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Adds a new category 'External Data' with new step 'Store in InfluxDB' to store data
 * in the time series database.
 *
 * Uses available Jersey RESTful client API in Quickbuild.
 */
@Editable(category = "External Data",
          name = "Store in InfluxDB",
          description = "This step will store the provided data to an InfluxDB instance.")
public class InfluxDbStep extends Step {
    private static final long serialVersionUID = 1L;

    private String database;
    private String data;

    @Editable(name="InfluxDB database name",
            description="Name of database in InfluxDB. Will be created during execution of step if it does not exist.")
    @NotEmpty
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String value) {
        database = value;
    }

    @Editable(name="Data", description="Data in line protocol format that will be stored.")
    @NotEmpty
    @Scriptable
    public String getData() {
        return data;
    }

    public void setData(String value) {
        data = value;
    }

    @Override
    public void run() {
        if (!hasDatabase(database)) {
            createDatabase(database);
        }
        storeData(getData());
    }

    /**
     * @param database Name of database we are interested in to check for existence.
     * @return true if database exists otherwise false
     */
    private boolean hasDatabase(String database) {
        WebResource resource = createResource("query");
        String databases = get(resource, "q", "SHOW DATABASES");
        return databases != null && databases.contains(database);
    }

    /**
     * Create database in InfluxDB.
     * @param database Name of database we want to create.
     */
    private void createDatabase(String database) {
        WebResource resource = createResource("query");
        get(resource, "q", "CREATE DATABASE " + database);
    }

    /**
     * Store data defined in Quickbuild step.
     * @param data provided in Quickbuild step.
     */
    private void storeData(String data) {
        WebResource resource = createResource("write");
        post(resource, "db", database, data);
    }

    /**
     * Create a resource at specific HTTP API endpoint. This resource can be than
     * used for either GET or POST requests.
     * @param endpoint Name of HTTP API endpoint (eg. query or write).
     * @return resource on which HTTP requests with URL parameters can be invoked.
     */
    private WebResource createResource(String endpoint) {
        InfluxDbSetting setting = (InfluxDbSetting) getPlugin().getConfigurationSetting(Context.getConfiguration(), true);
        Client client = Client.create();
        return client.resource(setting.getUrl() + "/" + endpoint);
    }

    /**
     * Sends a GET HTTP request to InfluxDB and returns the corresponding result.
     *
     * @param resource The HTTP API endpoint on which the request is executed.
     * @param paramKey Key of the URL parameter (eg. 'q').
     * @param paramValue Value of the URL parameter key (eg. 'CREATE DATABASE mydb').
     * @return response from HTTP API as string.
     * @throws QuickbuildException if response status is not 200 (OK)
     */
    private String get(WebResource resource, String paramKey, String paramValue) {
        ClientResponse response = resource.queryParam(paramKey, paramValue).get(ClientResponse.class);
        Context.getLogger().info(response.toString());

        if (response.getStatus() != 200) {
            throw new QuickbuildException("Error while sending query.");
        }
        return response.getEntity(String.class);
    }

    /**
     * Sends a POST HTTP request to InfluxDB.
     *
     * @param resource The HTTP API endpoint on which the request is executed.
     * @param paramKey Key of the URL parameter (eg. 'q').
     * @param paramValue Value of the URL parameter key (eg. 'CREATE DATABASE mydb').
     * @param data
     * @throws QuickbuildException if response status is not 204 (InfluxDB successfully executed the request)
     */
    private void post(WebResource resource, String paramKey, String paramValue, Object data) {
        ClientResponse response = resource.queryParam(paramKey, paramValue).type(MediaType.TEXT_PLAIN).
                post(ClientResponse.class, data);
        Context.getLogger().info(response.toString() + " with data " + data);

        if (response.getStatus() != 204) {
            throw new QuickbuildException("Bad request. Probably invalid data provided");
        }
    }
}
