#!/usr/bin/python

# MIT License
#
# Copyright (c) 2017 David Krebs
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import sys, getopt, os, subprocess, re
import time
from tqdm import *
from multiprocessing import Pool

def show_help():
  print """Usage: <script> -p <project> -b <bug> [OPTIONS]
Valid options are:
-p <project>	Specify a defects4j project you want to test [Lang|Math|...]
-b <bug>	Pass the bug number it should test [1...]
-w <path>	Workspace: Path where the result files and the checked out source files are written
-t <path>	Target: Were to expect the .dot result files
-f		Only run the failing testcase(s)
-h		Print this help text
"""
# Run a command in the shell
def cmd(cmd):
  try:
    return subprocess.check_output(cmd, shell=True)
  except CalledProcessError as e:
    print "Error in command: " + e

# Return all failing tests in that version
def failing_tests(proj, bug):
  output = cmd("defects4j info -p {0} -b {1}".format(proj,bug))
  tests = re.findall("""Root cause in triggering tests:
(.*)
--------------------------------------------------------------------------------
List of modified sources:""", output, re.DOTALL)[0].split('\n')
  tests = filter(lambda x: x.startswith(' - '), tests)
  return map(lambda x: x.replace(' - ', ''), tests)



# Clean the target directory from files of previous runs
def clean(target):
  files = os.listdir(target)
  for file in files:
    ext = os.path.splitext(file)[1]
    if ext in ['.png','.dot','.log']:
      os.remove(os.path.join(target,file))


# Checkout the given project and bug number
def checkout(target, proj, bug):
  dir = "{0}/{1}/{2}b".format(target, proj, bug)
  if not os.path.exists(dir):
    os.makedirs(dir)
  cmd("defects4j checkout -p {0} -v {1}b -w {2}".format(proj,bug,dir))


# Run the tests
def run(target, proj, bug, onlyFailing):
  if onlyFailing:
    for t in failing_tests(proj, bug):
      cmd("defects4j test -w {0}/{1}/{2}b -t {3}".format(target,proj,bug,t))
  else:
    cmd("defects4j test -w {0}/{1}/{2}b".format(target,proj,bug))


# Transform the graph results to images
def transform(target):
  files = os.listdir(target)
  files = map(os.path.splitext, files)
  files = filter(lambda x: x[1] == ".dot", files)
  files = map(lambda x: target + "/" + x[0], files)
  files = filter(lambda x: os.stat(x + ".dot") > 0, files)
  files = map(lambda x: "dot -Tpng '{0}.dot' -o '{0}.png'".format(x), files)

  pool = Pool(10)
  with tqdm(total=len(files)) as pbar:
    for i, _ in tqdm(enumerate(pool.imap_unordered(cmd, files))):
      pbar.update()
  pbar.close()
  pool.close()
  pool.join()



def main(argv):
  target = os.getcwd()
  workdir = os.getcwd()
  proj = None
  bug = None
  onlyFailing = False
  try:
    opts, args = getopt.getopt(argv,"hp:b:w:t:f")
  except getopt.GetoptError:
    print "Error in your arguments\n"
    show_help()
    sys.exit(2)
  for opt, arg in opts:
    if opt == '-h':
      show_help()
      sys.exit()
    elif opt == '-p':
      proj = arg
    elif opt == '-b':
      bug = arg
    elif opt == '-f':
      onlyFailing = True
    elif opt == '-w':
      workdir = arg
    elif opt == '-t':
      target = arg
  if proj == None or bug == None:
    print "Please specify a project and bug number\n"
    show_help()
    sys.exit(2)

#  clean(target)
#  checkout(workdir, proj, bug)
#  run(workdir, proj, bug, onlyFailing)
  transform(target)


if __name__ == "__main__":
  main(sys.argv[1:])
