# WhatsIllustrator

## What is WhatsIllustrator?
WhatsIllustrator is a program and framework which converts a WhatsApp chat into a pdf, odt and tex. It is written in Java and can be easily extended for other output formats or layouts.

## Getting started
### Compile
First, clone the repository using git (recommended):

```bash
git clone https://github.com/Terge3141/WhatsIllustrator.git
``` 

Get the emojis from noto-emoji using git (recommended)

```bash
git clone https://github.com/googlei18n/noto-emoji.git
``` 

Make sure that *Java 8* and *ant* is installed

Go to the WhatsIllustrator directory and type
```bash
ant
```
This will create the file dist.jar in the same directory.

### Run
Create a directory (from now on referred as $dir)

Export a chat from WhatsApp and save it to the directory ($dir/chat). This directory should contain a .txt file which usually has the name "WhatsApp Chat with The Nickname.txt" and some images (if any in the chat).

```bash
java -cp dist.jar thebook.Program -i $dir -e /path/to/noto-emoji/png/128
```

This will create the directory "output" in $dir. It has the following subdirectories:

* **fo**: Contains the generated **pdf** file.
* **odf**: Contains the generated **odt** file. It can be open for example with *libre office*.
* **tex** Contains the generated **tex** file and some files the tex document references. The file can be processed for example with *pdflatex*

## Image Pools
For some chats it appears not be possible to export them with media. In this case an `<`Media omitted`>` line occurs in the chat file. In this case WhatsBookSharp automatically searches the image pool directory and lists all possible images for the date of interest.

The match file is written to $dir. This can be editted written to the directory $dir/config. When WhatsBooksharp is invoked next time, it read match file and uses it to match $lt`<`Media omitted`>` messages.

The image pool directory should contain *all* images. It is provided as with the command line argument --imagepooldir.
