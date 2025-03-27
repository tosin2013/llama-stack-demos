# Deploy GitHub MCP Server on OpenShift Container Platform

This document provides instructions for deploying the GitHub MCP server on the OpenShift Container Platform.

## Official GitHub MCP Server Repository

You can find the official GitHub MCP server repository here: [GitHub MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/github)

## Deployment Steps

1. Run the deployment script `3_simple_agent_with_githubmcp.py`.
2. Verify the output, which should include a reference to the following image:

    ![Deployment Image](./images/image.png)

## Creating a Personal Access Token

To interact with GitHub, you need to create a Personal Access Token with the appropriate permissions:

1. Navigate to **GitHub Settings > Developer settings > Personal access tokens**.
2. Choose the repositories this token should access:
     - Public repositories
     - All repositories
     - Specific repositories (select manually)
3. Assign the required scopes:
     - For full control of private repositories, select the `repo` scope.
     - For public repositories only, select the `public_repo` scope.
4. Generate the token and copy it for use in the deployment process.

> **Note:** Keep your Personal Access Token secure and do not share it publicly.
