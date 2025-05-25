import openai
import os
import sys

# enable utf-8, so we don't get wierd
# crashes from French symbols
sys.stdout.reconfigure(encoding='utf-8')

# Set your OpenAI API key here or use environment variable
client = openai.OpenAI(api_key=os.getenv("OPENAI_API_KEY", "set your key"))  # Replace if needed

def ask_chatgpt(prompt: str, model: str = "gpt-4") -> str:
    chat_completion = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": """
            You are a helpful assistant, who gives brief answers on the given questions.
            
            For example:
            Question: At which temperature does the water boil?
            Answer: 100 degrees Celsius
            
            Question: Which city is a capital of France?
            Answer: Paris
            """},
            {"role": "user", "content": f"""
            Question:{prompt}
            Answer: 
            """},
        ]
    )
    return chat_completion.choices[0].message.content.strip()

# Example usage
if __name__ == "__main__":
    user_prompt = f'{" ".join(sys.argv[1:])}'
    reply = ask_chatgpt(user_prompt)
    print(reply)
