rm -rf output sample.mp4
python3 createjpgs.py
ffmpeg -i output/image%03d.jpg sample.mp4
