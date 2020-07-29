# efiling-hub-api

## Maven Profiles

### Openshift

Current image build in openshift runs with `openshift` profile by default.
This profile mostly enables splunk logging.

```bash
mvn install -P openshift
```

### Demo

This profile self isolate the application from any third party dependencies

```bash
mvn install -P demo
```

## Configuration

You should use environment variables to configure the jag efiling api

| Environment Variable | Type    | Description                 | Notes               |
| -------------------- | ------- | --------------------------- | ------------------- |
| SERVER_PORT          | Integer | web application server port | defaulted to `8081` |

## Backend Folder Structure

The backend API will follow the standard Java Spring Boot MVC model for folder structure breakdown where there are `models` and `controllers`.