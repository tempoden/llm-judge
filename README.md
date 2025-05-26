# llm-judge

![Build](https://github.com/tempoden/llm-judge/workflows/Build/badge.svg)

## Description

<!-- Plugin description -->
This plugin allows you to run evaluation of your models with a LLM-judge right from your IDE!

This plugin expects that you have access to the [api.openai.com](http://api.openai.com) and 
you API key is set to the `OPENAI_API_KEY` environment variable.

To use it, you should provide a data file with the following JSON format:

```json
{
  "model_path": "path/to/model.py",
  "data": [
    {
      "input": "german word for pleasure from someone else's pain",
      "reference_output": "Schadenfreude"
    },
    {
      "input": "who sang it must have been love but its over now",
      "reference_output": "Roxette"
    }
  ]
}
```

The `model.py` will be run using the chosen Python interpreter (by default, it will be the `python` executable from your `$PATH` environmental variable).

It is expected that the model receives input via argv and prints output to stdout.
The easiest way to do it is to use `" ".join(sys.argv[1:])` as the model input.
Also, you should remember about encoding, and it is good to set it to UTF-8
in your python script using `sys.stdout.reconfigure(encoding='utf-8')`.

<!-- Plugin description end -->

## Example models and data

The example datasets could be found in [misc/dataset](misc/dataset).
They are based on google's [natural-questions dataset](https://github.com/google-research-datasets/natural-questions/blob/master/nq_open/NQ-open.dev.jsonl).

The example local models could be found in [misc/model](misc/model).

## Installation

Currently, the plugin is available only for the manual installation:

Download the [latest release](https://github.com/tempoden/llm-judge/releases/latest) and install it manually using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Author: Denis Parfenov
tempoden@yandex.ru / tempodennsk@gmail.com

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
