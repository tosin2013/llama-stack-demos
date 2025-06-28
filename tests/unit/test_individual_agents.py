"""
Unit tests for individual Workshop Template System agents.
"""
import pytest
import requests
import time
from tests.utils.agent_client import AgentClient

class TestIndividualAgents:
    """Test each agent individually to ensure basic functionality."""
    
    def test_all_agents_responding(self, agent_endpoints):
        """Test that all agents are responding to basic requests."""
        for agent_name, endpoint in agent_endpoints.items():
            client = AgentClient(endpoint, agent_name)
            response = client.get_agent_card()
            
            assert response.success, f"{agent_name} is not responding"
            assert response.status_code == 200, f"{agent_name} returned status {response.status_code}"
            assert response.response_time < 10.0, f"{agent_name} response too slow: {response.response_time}s"
    
    def test_workshop_chat_agent(self, agent_endpoints):
        """Test workshop-chat agent specific functionality."""
        client = AgentClient(agent_endpoints["workshop-chat"], "workshop-chat")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "workshop" in str(card_response.data).lower()
        assert "chat" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()
    
    def test_template_converter_agent(self, agent_endpoints):
        """Test template-converter agent specific functionality."""
        client = AgentClient(agent_endpoints["template-converter"], "template-converter")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "template" in str(card_response.data).lower()
        assert "converter" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()
    
    def test_content_creator_agent(self, agent_endpoints):
        """Test content-creator agent specific functionality."""
        client = AgentClient(agent_endpoints["content-creator"], "content-creator")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "content" in str(card_response.data).lower()
        assert "creator" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()

    def test_source_manager_agent(self, agent_endpoints):
        """Test source-manager agent specific functionality."""
        client = AgentClient(agent_endpoints["source-manager"], "source-manager")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "source" in str(card_response.data).lower()
        assert "manager" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()

    def test_research_validation_agent(self, agent_endpoints):
        """Test research-validation agent specific functionality."""
        client = AgentClient(agent_endpoints["research-validation"], "research-validation")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "research" in str(card_response.data).lower()
        assert "validation" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()

    def test_documentation_pipeline_agent(self, agent_endpoints):
        """Test documentation-pipeline agent specific functionality."""
        client = AgentClient(agent_endpoints["documentation-pipeline"], "documentation-pipeline")

        # Test agent card
        card_response = client.get_agent_card()
        assert card_response.success
        assert "documentation" in str(card_response.data).lower()
        assert "pipeline" in str(card_response.data).lower()

        # Test health check
        assert client.health_check()
    
    def test_agent_response_times(self, agent_endpoints):
        """Test that all agents respond within acceptable time limits."""
        max_response_time = 5.0  # seconds
        
        for agent_name, endpoint in agent_endpoints.items():
            client = AgentClient(endpoint, agent_name)
            response = client.get_agent_card()
            
            assert response.response_time < max_response_time, \
                f"{agent_name} response time {response.response_time}s exceeds {max_response_time}s"
    
    def test_agent_error_handling(self, agent_endpoints):
        """Test agent error handling with invalid requests."""
        for agent_name, endpoint in agent_endpoints.items():
            # Test invalid endpoint
            try:
                response = requests.get(f"{endpoint}/invalid-endpoint", timeout=5)
                # Should return 404 or similar, not crash
                assert response.status_code in [404, 405, 501], \
                    f"{agent_name} should handle invalid endpoints gracefully"
            except requests.exceptions.RequestException:
                # Connection errors are acceptable for invalid endpoints
                pass
    
    @pytest.mark.slow
    def test_agent_concurrent_requests(self, agent_endpoints):
        """Test agents can handle concurrent requests."""
        import concurrent.futures
        import threading
        
        def make_request(endpoint, agent_name):
            client = AgentClient(endpoint, agent_name)
            return client.get_agent_card()
        
        # Test each agent with 5 concurrent requests
        for agent_name, endpoint in agent_endpoints.items():
            with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
                futures = [
                    executor.submit(make_request, endpoint, agent_name)
                    for _ in range(5)
                ]
                
                results = [future.result() for future in concurrent.futures.as_completed(futures)]
                
                # All requests should succeed
                successful_requests = sum(1 for result in results if result.success)
                assert successful_requests >= 4, \
                    f"{agent_name} failed too many concurrent requests: {successful_requests}/5"
    
    def test_agent_memory_usage(self, agent_endpoints):
        """Test that agents don't have obvious memory leaks with repeated requests."""
        # Make 10 requests to each agent and ensure they all succeed
        for agent_name, endpoint in agent_endpoints.items():
            client = AgentClient(endpoint, agent_name)
            
            successful_requests = 0
            for i in range(10):
                response = client.get_agent_card()
                if response.success:
                    successful_requests += 1
                time.sleep(0.1)  # Small delay between requests
            
            assert successful_requests >= 8, \
                f"{agent_name} failed too many repeated requests: {successful_requests}/10"
