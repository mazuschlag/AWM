import subprocess
import os

def main():
	classpath = os.getcwd() + r"\lib\system-hook-2.5.jar"
	print(classpath)
	os.chdir("bin")
	subprocess.check_call("java -cp .;" + classpath + " AWMs.Server", shell=True)

if __name__ == "__main__":
	main()