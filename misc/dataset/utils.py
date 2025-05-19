import json
import random

def readNQ(path):
    with open(path) as f:
        return  [json.loads(line) for line in f]

def nqToReq(lines):
    data = {
        "model_path": "smth",
        "data": list()
    }

    for line in lines:
        answers  = line.get("answer", [])
        selected_answer = answers[0] if answers else ""

        data["data"].append(
            {
                "input": line.get("question", ""),
                "reference_output": selected_answer
            }
        )

    return data

if __name__ == '__main__':
    n = 10
    data = random.sample(readNQ('./NQ-open.dev.jsonl'), n)
    data = nqToReq(data)

    with open(f'./demo-{n}.json', 'w', encoding='utf-8') as out:
        json.dump(data, out, indent=4)
