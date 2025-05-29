import os
import json
import time
from typing import Dict, List, Any, Optional
from llama_stack_client import LlamaStackClient
from llama_stack_client.lib.agents.agent import Agent
import tools as tool
import utils
from dotenv import load_dotenv

load_dotenv()

def load_queries(file_path: str) -> List[Dict[str, str]]:
    """Load query strings from a JSON file."""
    try:
        with open(file_path, 'r') as file:
            data = json.load(file)

        if not isinstance(data, dict) or 'queries' not in data:
            raise ValueError(f"Invalid JSON format in {file_path}")

        # Return full query objects with ID for better test identification
        return data['queries']
    except FileNotFoundError:
        print(f"Query file not found: {file_path}")
        return []
    except json.JSONDecodeError:
        print(f"Invalid JSON in file: {file_path}")
        return []


def get_query_id(query_obj):
    """Extract an ID from a query object for better test identification."""
    if isinstance(query_obj, dict) and 'id' in query_obj:
        return query_obj['id']
    elif isinstance(query_obj, dict) and 'query' in query_obj:
        # Use first few words of query if no ID is available
        words = query_obj['query'].split()[:5]
        return '_'.join(words).lower().replace(',', '').replace('.', '')
    return "unknown_query"


def execute_query(
    client: LlamaStackClient,
    prompt: str,
    model: str,
    instructions: Optional[str] = None,
    max_tokens: int = 4096
) -> Dict[str, Any]:
    """Execute a single query."""
    if instructions is None:
        instructions = """You are a helpful assistant. You MUST use a tool call to answer the user request, REGARDLESS
        of how simple or basic it may be.
            """

    agent = Agent(
        client,
        model=model,
        instructions=instructions,
        tools=[tool.add_two_numbers,
               tool.subtract_two_numbers,
               tool.multiply_two_numbers,
               tool.divide_two_numbers,
               tool.get_current_date,
               tool.greet_user,
               tool.string_length,
               tool.to_uppercase,
               tool.to_lowercase,
               tool.reverse_string,
               tool.is_even,
               tool.is_odd,
               tool.get_max_of_two,
               tool.get_min_of_two,
               tool.concatenate_strings,
               tool.convert_celsius_to_fahrenheit,
               tool.convert_fahrenheit_to_celsius,
               tool.is_palindrome,
               tool.calculate_square_root,
               tool.power,
               tool.get_day_of_week,
               tool.email_validator,
               tool.count_words,
               tool.average_two_numbers,
               tool.remove_whitespace,
               tool.celsius_to_kelvin,
               tool.fahrenheit_to_kelvin,
               tool.get_substring,
               tool.round_number,
               tool.is_leap_year,
               tool.generate_random_integer,
               tool.get_file_extension,
               # tool.replace_substring,
               # tool.is_prime,
               # tool.calculate_bmi,
               # tool.convert_kilograms_to_pounds,
               # tool.convert_pounds_to_kilograms,
               # tool.convert_feet_to_meters,
               # tool.is_alphanumeric,
               # tool.url_encode,
               # tool.url_decode
               ],
        sampling_params={"max_tokens": max_tokens}
    )

    # Create session
    session_id = agent.create_session(session_name=f"test-session-{model}-{int(time.time())}")
    print(f"Created session_id={session_id} for Agent({agent.agent_id})")

    # Execute query
    turn_response = agent.create_turn(
        messages=[
            {
                "role": "user",
                "content": prompt
            }
        ],
        session_id=session_id,
        stream=False
    )

    return turn_response


def run_test(model, query_obj, llama_client, logger):
    """Run a single test for a specific server type, model, and query."""
    query_id = get_query_id(query_obj)
    prompt = query_obj['query']
    expected_tool_call = query_obj['tool_call']


    logger.info(f"Testing query '{query_id}' with model {model}")
    logger.info(f"Query: {prompt[:50]}...")

    try:
        response = execute_query(
            llama_client,
            prompt,
            model
        )
        # Get Tool execution and Inference steps
        steps = response.steps

        #Get tool used
        try:
            tools_used = steps[1].tool_calls[0].tool_name
        except Exception as e:
            logger.error(f"Error extracting tool name: {e}")
            tools_used = None
        tool_call_match = True if tools_used == expected_tool_call else False
        logger.info(f"Tool used: {tools_used} Tool expected: {expected_tool_call} match: {tool_call_match} ")

        #Check inference was not empty
        final_response = ""
        try:
            final_response = steps[2].api_model_response.content.strip()
            inference_not_empty = True if final_response != '' else False
        except Exception as e:
            logger.error(f"Error checking inference content: {e}")
            inference_not_empty = False
        logger.info(f'Inference not empty: {inference_not_empty}')
        logger.info(f"Query '{query_id}' succeeded with model {model} and the response \n {final_response}")

        # Record success metrics, including the expected_tool_call
        utils.add_metric(
            model=model,
            query_id=query_id,
            status="SUCCESS",
            tool_call_match=tool_call_match,
            inference_not_empty=inference_not_empty,
            expected_tool_call=expected_tool_call # Pass the expected tool call here
        )

        return True

    except Exception as e:
        error_msg = str(e)
        logger.error(f"Query '{query_id}' failed with model {model}: {error_msg}")

        # Record failure metrics
        utils.add_metric(
            model=model,
            query_id=query_id,
            status="FAILURE",
            tool_call_match=False,
            inference_not_empty=False,
            expected_tool_call=expected_tool_call, # Pass the expected tool call here
            error=error_msg
        )

        return False


def main():
    """Main function to run all tests."""
    logger = utils.setup_logger()
    query_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), "../", "queries.json")
    base_url = os.getenv('REMOTE_BASE_URL')

    llama_client = LlamaStackClient(
        base_url=base_url,
    )

    models = ["meta-llama/Llama-3.2-3B-Instruct", ]
            #  "granite3.2:8b",]
            #   "watt-ai/watt-tool-8B",
            #   "meta-llama/Llama-3.3-70B-Instruct"]

    total_tests = 0
    successful_tests = 0

    for model in models:
        logger.info(f"\n=== Testing with model: {model} ===\n")

        queries = load_queries(query_path)
        if not queries:
            logger.info(f"No queries found")
            continue

        logger.info(f"Running {len(queries)} queries")

        for query_obj in queries:
            total_tests += 1
            success = run_test(model, query_obj, llama_client, logger)
            if success:
                successful_tests += 1

    logger.info(f"\n=== Test Summary ===")
    logger.info(f"Total tests: {total_tests}")
    logger.info(f"Successful tests: {successful_tests}")
    logger.info(f"Failed tests: {total_tests - successful_tests}")
    if total_tests > 0:
        success_rate = (successful_tests / total_tests) * 100
        logger.info(f"Success rate: {success_rate:.1f}%")

    utils.get_analysis_plots()


if __name__ == "__main__":
    main()
