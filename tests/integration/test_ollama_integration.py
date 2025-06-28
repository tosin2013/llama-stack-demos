"""
Integration tests for Ollama LLM functionality with Workshop Template System.
"""
import pytest
import requests
import json
from tests.utils.agent_client import AgentClient

@pytest.mark.ollama
class TestOllamaIntegration:
    """Test integration with Ollama for real LLM responses."""

    def get_llm_model(self, ollama_available):
        """Get the appropriate LLM model for text generation."""
        models = ollama_available["models"]
        # Prefer llama models for text generation
        for model in models:
            if "llama" in model.lower() and "instruct" in model.lower():
                return model
        # Fall back to any non-embedding model
        for model in models:
            if "minilm" not in model.lower() and "embed" not in model.lower():
                return model
        # Last resort - use any available model
        return models[0] if models else None
    
    def test_ollama_availability(self, ollama_available):
        """Test that Ollama is available and has models."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available - skipping LLM integration tests")
        
        assert len(ollama_available["models"]) > 0, "No models available in Ollama"
        print(f"✅ Ollama available with {len(ollama_available['models'])} models")
    
    def test_ollama_model_list(self, ollama_available):
        """Test retrieving available models from Ollama."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        response = requests.get(f"{endpoint}/api/tags")
        
        assert response.status_code == 200
        data = response.json()
        assert "models" in data
        assert len(data["models"]) > 0
        
        print(f"Available models: {[m['name'] for m in data['models']]}")
    
    def test_ollama_simple_generation(self, ollama_available):
        """Test simple text generation with Ollama."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")

        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)
        
        payload = {
            "model": model,
            "prompt": "What is a workshop?",
            "stream": False
        }
        
        response = requests.post(f"{endpoint}/api/generate", json=payload, timeout=30)
        
        assert response.status_code == 200
        data = response.json()
        assert "response" in data
        assert len(data["response"]) > 0
        
        print(f"✅ Ollama generated response: {data['response'][:100]}...")
    
    def test_workshop_specific_prompts(self, ollama_available):
        """Test Ollama with workshop-specific prompts."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)
        
        workshop_prompts = [
            "How would you structure a technical workshop?",
            "What are the key components of a hands-on coding workshop?",
            "Explain how to create engaging learning objectives for a workshop."
        ]
        
        for prompt in workshop_prompts:
            payload = {
                "model": model,
                "prompt": prompt,
                "stream": False
            }
            
            response = requests.post(f"{endpoint}/api/generate", json=payload, timeout=30)
            
            assert response.status_code == 200
            data = response.json()
            assert "response" in data
            assert len(data["response"]) > 10  # Should be substantial response
            
            print(f"✅ Workshop prompt response length: {len(data['response'])} chars")
    
    def test_repository_analysis_prompts(self, ollama_available, test_repositories):
        """Test Ollama with repository analysis prompts."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)
        test_repo = test_repositories[0]
        
        analysis_prompt = f"""
        Analyze this repository for workshop creation:
        Repository: {test_repo['name']}
        URL: {test_repo['url']}
        Type: {test_repo['type']}
        
        What would be good learning objectives for a workshop based on this repository?
        """
        
        payload = {
            "model": model,
            "prompt": analysis_prompt,
            "stream": False
        }
        
        response = requests.post(f"{endpoint}/api/generate", json=payload, timeout=45)
        
        assert response.status_code == 200
        data = response.json()
        assert "response" in data
        
        # Response should mention workshops or learning
        response_text = data["response"].lower()
        assert any(keyword in response_text for keyword in ["workshop", "learn", "objective", "skill"]), \
            "Response should be workshop-related"
        
        print(f"✅ Repository analysis response generated successfully")
    
    @pytest.mark.slow
    def test_agent_ollama_integration(self, ollama_available, agent_endpoints):
        """Test agents with Ollama integration (if supported)."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        # Note: Our current agents use mock responses
        # This test validates the interface for future Ollama integration
        
        for agent_name, endpoint in agent_endpoints.items():
            client = AgentClient(endpoint, agent_name)
            
            # Send a message that would benefit from LLM processing
            llm_message = f"Using LLM capabilities, analyze the structure of a Python project for workshop creation"
            
            response = client.send_message(llm_message)
            
            # Current mock agents should handle this gracefully
            assert response.status_code in [200, 404, 501], \
                f"Agent {agent_name} should handle LLM-style requests"
            
            print(f"✅ Agent {agent_name} handled LLM-style request")
    
    def test_ollama_workshop_content_generation(self, ollama_available):
        """Test generating workshop content with Ollama."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)

        content_prompt = """
        Create a workshop module outline for teaching Python basics.
        Include:
        1. Learning objectives
        2. Key concepts to cover
        3. Hands-on exercises
        4. Assessment criteria
        
        Format as a structured outline.
        """
        
        payload = {
            "model": model,
            "prompt": content_prompt,
            "stream": False
        }
        
        response = requests.post(f"{endpoint}/api/generate", json=payload, timeout=60)
        
        assert response.status_code == 200
        data = response.json()
        assert "response" in data
        
        content = data["response"]
        
        # Validate content structure
        assert len(content) > 100, "Content should be substantial"
        
        # Should contain workshop-related terms
        content_lower = content.lower()
        expected_terms = ["objective", "exercise", "concept", "learn"]
        found_terms = sum(1 for term in expected_terms if term in content_lower)
        assert found_terms >= 2, "Content should contain workshop-related terms"
        
        print(f"✅ Generated workshop content ({len(content)} chars)")
    
    def test_ollama_error_handling(self, ollama_available):
        """Test Ollama error handling with invalid requests."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        
        # Test with invalid model
        invalid_payload = {
            "model": "nonexistent-model",
            "prompt": "Test prompt",
            "stream": False
        }
        
        response = requests.post(f"{endpoint}/api/generate", json=invalid_payload, timeout=10)
        
        # Should return error status
        assert response.status_code in [400, 404, 500], "Should handle invalid model gracefully"
        
        print("✅ Ollama error handling test completed")
    
    def test_ollama_streaming_response(self, ollama_available):
        """Test Ollama streaming responses."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)

        payload = {
            "model": model,
            "prompt": "Explain the benefits of hands-on workshops in 3 sentences.",
            "stream": True
        }
        
        response = requests.post(f"{endpoint}/api/generate", json=payload, stream=True, timeout=30)
        
        assert response.status_code == 200
        
        # Collect streaming response
        chunks = []
        for line in response.iter_lines():
            if line:
                try:
                    chunk_data = json.loads(line)
                    if "response" in chunk_data:
                        chunks.append(chunk_data["response"])
                    if chunk_data.get("done", False):
                        break
                except json.JSONDecodeError:
                    continue
        
        # Should have received multiple chunks
        assert len(chunks) > 0, "Should receive streaming chunks"
        
        # Combine chunks to get full response
        full_response = "".join(chunks)
        assert len(full_response) > 10, "Streaming response should be substantial"
        
        print(f"✅ Streaming response received ({len(chunks)} chunks, {len(full_response)} chars)")
    
    @pytest.mark.slow
    def test_ollama_performance_benchmark(self, ollama_available):
        """Benchmark Ollama performance for workshop use cases."""
        if not ollama_available["available"]:
            pytest.skip("Ollama not available")
        
        import time
        
        endpoint = ollama_available["endpoint"]
        model = self.get_llm_model(ollama_available)

        test_prompts = [
            "What is Python?",
            "Explain object-oriented programming in one paragraph.",
            "List 5 benefits of automated testing."
        ]
        
        response_times = []
        
        for prompt in test_prompts:
            start_time = time.time()
            
            payload = {
                "model": model,
                "prompt": prompt,
                "stream": False
            }
            
            response = requests.post(f"{endpoint}/api/generate", json=payload, timeout=60)
            
            response_time = time.time() - start_time
            response_times.append(response_time)
            
            assert response.status_code == 200
            
        avg_response_time = sum(response_times) / len(response_times)
        max_response_time = max(response_times)
        
        # Performance expectations (adjust based on your hardware)
        assert avg_response_time < 30, f"Average response time too slow: {avg_response_time:.2f}s"
        assert max_response_time < 60, f"Max response time too slow: {max_response_time:.2f}s"
        
        print(f"✅ Performance benchmark: avg={avg_response_time:.2f}s, max={max_response_time:.2f}s")
