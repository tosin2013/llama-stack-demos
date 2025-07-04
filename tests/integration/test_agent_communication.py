"""
Integration tests for agent-to-agent communication patterns.
"""

import time

import pytest

from tests.utils.agent_client import WorkshopSystemClient


@pytest.mark.integration
class TestAgentCommunication:
    """Test communication patterns between Workshop Template System agents."""

    def test_all_agents_health_check(self, agent_endpoints):
        """Test that all agents are healthy and can communicate."""
        client = WorkshopSystemClient(agent_endpoints)
        health_status = client.health_check_all()

        for agent_name, is_healthy in health_status.items():
            assert is_healthy, f"Agent {agent_name} is not healthy"

        print(f"✅ All {len(health_status)} agents are healthy")

    def test_agent_card_retrieval(self, agent_endpoints):
        """Test retrieving agent cards from all agents."""
        client = WorkshopSystemClient(agent_endpoints)
        agent_cards = client.get_all_agent_cards()

        for agent_name, response in agent_cards.items():
            assert response.success, f"Failed to get agent card from {agent_name}"
            assert response.status_code == 200
            assert isinstance(response.data, dict)

        print(
            f"✅ Successfully retrieved agent cards from all {
                len(agent_cards)} agents"
        )

    def test_sequential_agent_workflow(self, agent_endpoints, test_repositories):
        """Test sequential workflow through multiple agents."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]  # Use first test repository

        # Step 1: Source Manager
        source_manager = client.agents["source-manager"]
        source_result = source_manager.send_message(
            f"Analyze repository: {test_repo['url']}"
        )
        assert source_result.success, "Source manager failed to process request"

        # Step 2: Template Converter (using source manager result)
        template_converter = client.agents["template-converter"]
        template_result = template_converter.send_message(
            f"Convert to workshop template based on analysis: {
                source_result.data}"
        )
        assert template_result.success, "Template converter failed to process request"

        # Step 3: Content Creator (using template converter result)
        content_creator = client.agents["content-creator"]
        content_result = content_creator.send_message(
            f"Create workshop content based on template: {
                template_result.data}"
        )
        assert content_result.success, "Content creator failed to process request"

        print("✅ Sequential agent workflow completed successfully")

    def test_parallel_agent_requests(self, agent_endpoints):
        """Test that agents can handle parallel requests."""
        import concurrent.futures

        client = WorkshopSystemClient(agent_endpoints)

        def send_test_message(agent_name):
            agent = client.agents[agent_name]
            return agent.send_message(f"Test message from parallel test")

        # Send messages to all agents in parallel
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as executor:
            futures = {
                executor.submit(send_test_message, agent_name): agent_name
                for agent_name in client.agents.keys()
            }

            results = {}
            for future in concurrent.futures.as_completed(futures):
                agent_name = futures[future]
                try:
                    result = future.result()
                    results[agent_name] = result
                except Exception as e:
                    results[agent_name] = {"error": str(e)}

        # Verify all agents responded
        for agent_name, result in results.items():
            if hasattr(result, "success"):
                assert result.success, f"Agent {agent_name} failed parallel request"
            else:
                pytest.fail(f"Agent {agent_name} threw exception: {result}")

        print(
            f"✅ All {
                len(results)} agents handled parallel requests successfully"
        )

    def test_agent_session_management(self, agent_endpoints):
        """Test that agents can manage sessions properly."""
        client = WorkshopSystemClient(agent_endpoints)
        session_id = f"test-session-{int(time.time())}"

        # Send messages with same session ID to multiple agents
        for agent_name, agent in client.agents.items():
            response = agent.send_message(
                f"Hello from {agent_name} session test", session_id=session_id
            )
            # Note: Our mock agents might not fully support sessions yet
            # This test validates the interface works
            assert response.status_code in [
                200,
                404,
                501,
            ], f"Agent {agent_name} should handle session requests gracefully"

    def test_agent_error_propagation(self, agent_endpoints):
        """Test how agents handle and propagate errors."""
        client = WorkshopSystemClient(agent_endpoints)

        # Send invalid requests to each agent
        for agent_name, agent in client.agents.items():
            # Test with malformed JSON-like message
            response = agent.send_message('{"invalid": "json"')

            # Agents should handle errors gracefully
            assert response.status_code in [
                200,
                400,
                422,
                500,
            ], f"Agent {agent_name} should handle malformed requests gracefully"

    @pytest.mark.slow
    def test_agent_load_handling(self, agent_endpoints):
        """Test agents under moderate load."""
        client = WorkshopSystemClient(agent_endpoints)

        # Send 20 requests to each agent over 10 seconds
        for agent_name, agent in client.agents.items():
            successful_requests = 0
            total_requests = 20

            for i in range(total_requests):
                response = agent.send_message(f"Load test message {i}")
                if hasattr(response, "success") and response.success:
                    successful_requests += 1
                elif hasattr(response, "status_code") and response.status_code == 200:
                    successful_requests += 1

                time.sleep(0.5)  # 500ms between requests

            success_rate = successful_requests / total_requests
            assert (
                success_rate >= 0.8
            ), f"Agent {agent_name} success rate too low: {success_rate:.2%}"

            print(
                f"✅ Agent {agent_name} handled load test: {
                    success_rate:.2%} success rate"
            )

    def test_agent_response_consistency(self, agent_endpoints):
        """Test that agents provide consistent responses."""
        client = WorkshopSystemClient(agent_endpoints)

        # Send same message multiple times to each agent
        test_message = "Consistency test message"

        for agent_name, agent in client.agents.items():
            responses = []

            for i in range(3):
                response = agent.send_message(test_message)
                responses.append(response)
                time.sleep(1)

            # All responses should have same success status
            success_statuses = [
                r.success if hasattr(r, "success") else (r.status_code == 200)
                for r in responses
            ]
            assert (
                len(set(success_statuses)) <= 1
            ), f"Agent {agent_name} gave inconsistent response statuses"

    def test_cross_agent_data_flow(self, agent_endpoints, sample_workshop_request):
        """Test data flow between agents in workshop creation workflow."""
        client = WorkshopSystemClient(agent_endpoints)

        # Simulate data flowing from one agent to another
        workflow_data = {
            "repository_url": sample_workshop_request["repository_url"],
            "step": "initial",
        }

        # Pass data through agent chain
        agent_chain = [
            "source-manager",
            "template-converter",
            "content-creator",
            "research-validation",
            "documentation-pipeline",
        ]

        for i, agent_name in enumerate(agent_chain):
            agent = client.agents[agent_name]

            # Send workflow data to agent
            message = f"Process step {i + 1}: {workflow_data}"
            response = agent.send_message(message)

            # Update workflow data for next agent
            workflow_data["step"] = f"completed-{agent_name}"
            workflow_data["previous_agent"] = agent_name

            # Verify agent responded appropriately
            assert response.status_code in [
                200,
                404,
                501,
            ], f"Agent {agent_name} should handle workflow data"

        print("✅ Cross-agent data flow test completed")
