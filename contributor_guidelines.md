---
layout: default
title: Contributor Guidelines
---

# Coding Guidelines

## Functionality should have appropriate unit tests

Robolectric is a unit testing framework and it is important that Robolectric itself be very well tested. All classes should have unit test classes. All public methods should have unit tests. Those classes and methods should have their possible states well tested. Copied Android source should at least have "smoke tests" that assure the copied functionality is wired up correctly. Pull requests introducing untested functionality should not accepted, and reviewers should give appropriate feedback to the submitter.

## Code can be copied from Android source when appropriate

"When appropriate" is subjective. In an effort to avoid complexity, copying Android source as the basis for Shadow object functionality is discouraged. That said, sometimes the functionality is complex and the Android implementation is what is needed. Contributors and reviewers should use their best judgement: should a 3000-line Android class be copied to gain access to a boolean getter and setter? It depends.

## Follow the code formatting standard

This is essentially the IntelliJ default Java style, but with two-space indents.
* Spaces, not tabs.
* Indenting: http://en.wikipedia.org/wiki/Indent_style#Variant:_1TBS but with two spaces, not four.
* Curly braces for everything: if, else, etc.
* One line of white space between methods

# Git Submodules

If you are building an Android project that frequently needs changes to Robolectric, you probably want to maintain a fork of Robolectric from which you
can submit pull requests. One way to set things up is to use a git submodule to this fork from your main project.

[Android IntelliJ Starter](https://github.com/pivotal/AndroidIntelliJStarter)
is an Android project generator which configures Robolectric as a [git submodule](http://kernel.org/pub/software/scm/git/docs/git-submodule.html "git-submodule(1)").


