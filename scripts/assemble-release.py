#!/usr/bin/env python3
"""Build the real Gradle dependency graph, then sign with the existing rotation lineage.
Requires the two real BrickStorm ad unit IDs; never builds a demo production APK.
"""
import os,subprocess,shutil,hashlib
from pathlib import Path
root=Path(__file__).resolve().parents[1];os.chdir(root)
gradle=os.environ.get('BRICKSTORM_GRADLE','gradle')
args=[gradle]
if os.environ.get('BRICKSTORM_GRADLE_USER_HOME'):args+=['--gradle-user-home',os.environ['BRICKSTORM_GRADLE_USER_HOME']]
args+=['--no-daemon',':app:assembleRelease',':app:lintRelease']
subprocess.run(args,check=True)
bt=Path(os.environ['BUILD_TOOLS']);out=root/'out/release';out.mkdir(parents=True,exist_ok=True)
unsigned=root/'app/build/outputs/apk/release/app-release-unsigned.apk'
if not unsigned.is_file():raise SystemExit('Gradle unsigned release APK missing')
aligned=out/'aligned.apk';subprocess.run([str(bt/'zipalign'),'-P','16','-f','4',str(unsigned),str(aligned)],check=True)
command=['java','-jar',str(bt/'lib/apksigner.jar'),'sign','--debuggable-apk-permitted','false','--v4-signing-enabled','false']
old=os.environ.get('BRICKSTORM_PREVIOUS_KEYSTORE');lineage=os.environ.get('BRICKSTORM_LINEAGE')
if old and lineage:command+=['--ks',old,'--ks-key-alias',os.environ.get('BRICKSTORM_PREVIOUS_ALIAS','test'),'--ks-pass','env:BRICKSTORM_PREVIOUS_PASSWORD','--next-signer']
command+=['--ks',os.environ['BRICKSTORM_KEYSTORE'],'--ks-key-alias',os.environ.get('BRICKSTORM_KEY_ALIAS','brickstorm'),'--ks-pass','env:BRICKSTORM_KEY_PASSWORD']
if old and lineage:command+=['--lineage',lineage,'--rotation-min-sdk-version','28']
apk=out/'BrickStorm-1.14.1.apk';command+=['--out',str(apk),str(aligned)];subprocess.run(command,check=True)
subprocess.run(['java','-jar',str(bt/'lib/apksigner.jar'),'verify','--verbose','--print-certs',str(apk)],check=True)
subprocess.run([str(bt/'aapt2'),'dump','badging',str(apk)],check=True);subprocess.run([str(bt/'zipalign'),'-c','-P','16','4',str(apk)],check=True)
print('RELEASE_APK:',apk);print('SHA256:',hashlib.sha256(apk.read_bytes()).hexdigest())
