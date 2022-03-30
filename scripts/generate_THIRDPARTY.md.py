# Generates THIRDPARTY.md license report as gathered from pom.xml
# Usage:
#     pip install markdownify
#     python scripts/generate_THIRDPARTY.md.py
# 
# A THIRDPARTY.md file is generated at project's root folder.

import markdownify 
import subprocess
import sys

subprocess.run(['mvn', 'clean'])
subprocess.run(['mvn', 'project-info-reports:dependencies'])
html = open('target/site/dependencies.html').read()
h = markdownify.markdownify(html, heading_style="ATX")
with open('THIRDPARTY.md','w') as f:
    for line in h.splitlines():
        if 'Project Dependency Graph' in line:
            break
        print(line, file=f)
