# Devscribe
---

## Contributors:

1. Vladislav Artiukhov
2. Dmitrii Artiukhov


## Description:

Full-fledged IDE developed in Kotlin with support of syntax highlighting, file renaming/creation/deletion, and search functionalities | Semester project, 3rd year at Constructor University, 2023.


## Supported functionality:
- Caret movement via keyboard arrows and mouse clicks.
- Text typing and keyboard special characters support: `BACKSPACE`, `DELETE`, `CTRL+ARROW`.
- Optimized render via drawing only the text visible in the viewport.
- Vertical and Horizontal canvas scrolling.
- Text searching inside opened file and navigation to prev/next search result entry.
- Ability to open multiple files using tabs.
- VFS integration: opening project directory from OS, saving files on disk, file renaming, file deletion, file creation.
- Implementation of Lexer for a fictional programming language.
- Syntax highlighting.
- Navigation without mouse use (using tabs only).
- Selection via mouse drags and keyboard arrows movement.
- Copy/paste/cut functionality of selected text.


## Implementation details:
- Array of strings is used as text management data structure; interface allows seamless itegration of other structures.
- Syntax highlighting is implemented via tokens generated by Lexer.
- Support for an arbitrary number of VFS commands via **Command** pattern and execution pipeline using queue of commands and additional worker thread.


## How to build and run the project:
Pretty straight-forward: clone the project repo and build the project via Intellij IDE.
