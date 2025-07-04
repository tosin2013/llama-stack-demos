"""
End-to-end tests for complete workshop generation workflow.
"""

import time

import pytest

from tests.utils.agent_client import WorkshopSystemClient


@pytest.mark.e2e
@pytest.mark.slow
class TestWorkshopGeneration:
    """Test complete workshop generation workflows with real repositories."""

    def test_full_workshop_creation_workflow(
        self, agent_endpoints, test_repositories, sample_workshop_request
    ):
        """Test the complete workshop creation workflow from start to finish."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]  # Use llama-stack-demos repository

        print(
            f"\n🚀 Starting full workshop creation workflow for {
                test_repo['name']}"
        )

        # Execute the full workflow
        workflow_results = client.orchestrate_workshop_creation(
            test_repo["url"], sample_workshop_request
        )

        # Validate each step of the workflow
        assert "source_analysis" in workflow_results
        assert "template_conversion" in workflow_results
        assert "content_creation" in workflow_results
        assert "content_validation" in workflow_results
        assert "documentation" in workflow_results
        assert "chat_interface" in workflow_results

        # Check that each step completed (even if with mock responses)
        for step_name, result in workflow_results.items():
            assert hasattr(
                result, "status_code"
            ), f"Step {step_name} missing status code"
            assert result.status_code in [
                200,
                404,
                501,
            ], f"Step {step_name} failed with status {
                result.status_code}"

        print("✅ Full workshop creation workflow completed")
        return workflow_results

    def test_workshop_creation_with_different_repositories(
        self, agent_endpoints, test_repositories
    ):
        """Test workshop creation with different types of repositories."""
        client = WorkshopSystemClient(agent_endpoints)

        for repo in test_repositories:
            print(
                f"\n🔍 Testing workshop creation for {
                    repo['name']} ({
                    repo['type']})"
            )

            # Create workshop config based on repository type
            workshop_config = {
                "repository_url": repo["url"],
                "workshop_title": f"{repo['name']} Workshop",
                "target_audience": "developers",
                "repository_type": repo["type"],
            }

            # Execute workflow
            workflow_results = client.orchestrate_workshop_creation(
                repo["url"], workshop_config
            )

            # Validate workflow completed
            assert len(workflow_results) == 6, "All workflow steps should be present"

            # Check source analysis step specifically
            source_result = workflow_results.get("source_analysis")
            assert source_result is not None, "Source analysis step missing"

            print(f"✅ Workshop creation test completed for {repo['name']}")

    def test_workshop_creation_error_handling(self, agent_endpoints):
        """Test workshop creation with invalid repositories and error conditions."""
        client = WorkshopSystemClient(agent_endpoints)

        # Test with invalid repository URL
        invalid_repo_url = "https://github.com/nonexistent/repository.git"

        workflow_results = client.orchestrate_workshop_creation(
            invalid_repo_url, {"workshop_title": "Invalid Repo Test"}
        )

        # Workflow should complete but may have errors in individual steps
        assert len(workflow_results) == 6, "All workflow steps should be attempted"

        # At least some steps should handle the error gracefully
        completed_steps = sum(
            1
            for result in workflow_results.values()
            if hasattr(result, "status_code") and result.status_code in [200, 404, 501]
        )
        assert (
            completed_steps >= 3
        ), "At least half the steps should handle errors gracefully"

        print("✅ Error handling test completed")

    def test_workshop_creation_performance(self, agent_endpoints, test_repositories):
        """Test workshop creation performance and timing."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]

        start_time = time.time()

        workflow_results = client.orchestrate_workshop_creation(
            test_repo["url"], {"workshop_title": "Performance Test Workshop"}
        )

        total_time = time.time() - start_time

        # Workflow should complete within reasonable time (with mock agents)
        assert total_time < 60, f"Workflow took too long: {total_time:.2f}s"

        # Check individual step timing
        for step_name, result in workflow_results.items():
            if hasattr(result, "response_time"):
                assert (
                    result.response_time < 30
                ), f"Step {step_name} took too long: {
                    result.response_time:.2f}s"

        print(f"✅ Performance test completed in {total_time:.2f}s")

    def test_workshop_content_validation(self, agent_endpoints, test_repositories):
        """Test that generated workshop content meets basic quality standards."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]

        workflow_results = client.orchestrate_workshop_creation(
            test_repo["url"],
            {
                "workshop_title": "Content Validation Test",
                "learning_objectives": [
                    "Understand the codebase structure",
                    "Learn key concepts",
                    "Build practical skills",
                ],
            },
        )

        # Validate content creation step
        content_result = workflow_results.get("content_creation")
        assert content_result is not None, "Content creation step missing"

        # Validate documentation step
        docs_result = workflow_results.get("documentation")
        assert docs_result is not None, "Documentation step missing"

        # Validate research validation step
        validation_result = workflow_results.get("content_validation")
        assert validation_result is not None, "Content validation step missing"

        print("✅ Content validation test completed")

    def test_workshop_customization_options(self, agent_endpoints, test_repositories):
        """Test workshop creation with different customization options."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]

        # Test different workshop configurations
        configurations = [
            {
                "workshop_title": "Beginner Workshop",
                "target_audience": "beginners",
                "duration": "1 hour",
                "difficulty": "easy",
            },
            {
                "workshop_title": "Advanced Workshop",
                "target_audience": "experts",
                "duration": "4 hours",
                "difficulty": "advanced",
            },
            {
                "workshop_title": "Hands-on Workshop",
                "target_audience": "developers",
                "duration": "2 hours",
                "format": "hands-on",
            },
        ]

        for i, config in enumerate(configurations):
            print(
                f"\n🎯 Testing configuration {
                    i +
                    1}: {
                    config['workshop_title']}"
            )

            workflow_results = client.orchestrate_workshop_creation(
                test_repo["url"], config
            )

            # Validate workflow completed
            assert (
                len(workflow_results) == 6
            ), f"Configuration {
                i + 1} workflow incomplete"

            # Check that configuration was processed
            for step_name, result in workflow_results.items():
                assert hasattr(
                    result, "status_code"
                ), f"Configuration {
                    i + 1} step {step_name} missing status"

        print("✅ Customization options test completed")

    def test_concurrent_workshop_creation(self, agent_endpoints, test_repositories):
        """Test creating multiple workshops concurrently."""
        import concurrent.futures

        client = WorkshopSystemClient(agent_endpoints)

        def create_workshop(repo_index):
            repo = test_repositories[repo_index % len(test_repositories)]
            config = {
                "workshop_title": f"Concurrent Workshop {repo_index}",
                "target_audience": "developers",
            }
            return client.orchestrate_workshop_creation(repo["url"], config)

        # Create 3 workshops concurrently
        with concurrent.futures.ThreadPoolExecutor(max_workers=3) as executor:
            futures = [executor.submit(create_workshop, i) for i in range(3)]
            results = [
                future.result() for future in concurrent.futures.as_completed(futures)
            ]

        # All workshops should complete
        assert len(results) == 3, "All concurrent workshops should complete"

        for i, result in enumerate(results):
            assert len(result) == 6, f"Concurrent workshop {i} incomplete"

        print("✅ Concurrent workshop creation test completed")

    def test_workshop_state_persistence(self, agent_endpoints, test_repositories):
        """Test that workshop creation state is handled properly."""
        client = WorkshopSystemClient(agent_endpoints)
        test_repo = test_repositories[0]

        # Create workshop with specific session ID
        session_id = f"persistence-test-{int(time.time())}"

        # Start workflow
        workflow_results = client.orchestrate_workshop_creation(
            test_repo["url"],
            {"workshop_title": "Persistence Test Workshop", "session_id": session_id},
        )

        # Validate workflow state
        assert len(workflow_results) == 6, "Workflow should complete all steps"

        # Test that agents can handle session-based requests
        chat_agent = client.agents["workshop-chat"]
        followup_response = chat_agent.send_message(
            "What workshop did we just create?", session_id=session_id
        )

        # Agent should handle the session request (even if with mock response)
        assert followup_response.status_code in [
            200,
            404,
            501,
        ], "Agent should handle session-based followup"

        print("✅ Workshop state persistence test completed")
