# WhatsIllustrator

## What is WhatsIllustrator?
WhatsIllustrator is a program and framework which converts WhatsApp, Telegram Messenger and Signal Messenger chats into a pdf, odt and tex. It is written in Java and can be easily extended for other output formats, layouts and messengers.

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

Make sure that *Java 11* and *maven* is installed

Go to the WhatsIllustrator directory and build package
```bash
cd whatsillustrator
mvn package
```
This will create the file dist.jar in the same directory.

### Run
#### WhatsApp
1. Create a directory ($wadir)
2. Export a chat from WhatsApp and save it to the directory ($wadir/chat). This directory should contain a .txt file which usually has the name "WhatsApp Chat with The Nickname.txt" and some images (if any in the chat).

#### Telegram
1. Export a Telegram Chat: https://telegram.org/blog/export-and-more

#### Signal
1. Activate backup and copy backup file to your pc, see also https://support.signal.org/hc/en-us/articles/360007059752-Backup-and-Restore-Messages

#### All 
Open whatsillustrator/configurator/src/main/java/configurator/sampleconfig.xml, modify it and save it to as config.xml.

```bash
java -cp thebook/target/thebook-0.0.1-SNAPSHOT-jar-with-dependencies.jar\
	thebook.Program -c /path/to/config.xml
```

The reults will be written to *outputdir* (defined in config.xml). Each chat contains following subdirectories (if writers are activated):
* **fo**: Contains the generated **pdf** file.
* **odf**: Contains the generated **odt** file. It can be opened for example with *libre office*.
* **tex** Contains the generated **tex** file and some files the tex document references. The file can be processed for example with *pdflatex*

## Image Pools (WhatsApp only)
For some chats it appears that it is not possible to export them with media. Then a `<`Media omitted`>` line occurs in the chat file. In this case *whatsillustrator* automatically searches the image pool directory and lists all possible images for the date of interest.

The match file is written to chat output directory. This can be edited and written to the directory $wadir/config. When *whatsillustrator* is invoked next time, it reads the match file and uses it to match `<`Media omitted`>` messages.

The image pool directory should contain *all* images and configured as *imagepooldir* in the config.xml file (parserconfiguration).

# Contributions
Contributions (new features, bug fixes, changes) are very welcome. Just send send me the pull requests or patches. Also, if you find any bugs let me know.
