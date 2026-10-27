#!/usr/bin/env python3
"""Build one non-debuggable release APK using the official Android SDK.
Signing material is external to the repository; never generates a replacement key.
"""
import os,subprocess,sys,zipfile,shutil
from pathlib import Path
root=Path(__file__).resolve().parent.parent
os.chdir(root)
sdk=Path(os.environ['ANDROID_JAR']);bt=Path(os.environ['BUILD_TOOLS']);key=Path(os.environ['BRICKSTORM_KEYSTORE'])
alias=os.environ.get('BRICKSTORM_KEY_ALIAS','brickstorm');os.environ['BRICKSTORM_KEY_PASSWORD']
out=root/'out/release';classes=out/'classes'
if classes.exists():shutil.rmtree(classes)
classes.mkdir(parents=True,exist_ok=True)
def run(args):subprocess.run([str(v) for v in args],check=True)
sources=sorted((root/'app/src/main/java').rglob('*.java'))
run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-encoding','UTF-8','-source','17','-target','17','-classpath',sdk,'-d',classes,*sources])
run([bt/'aapt2','compile','--dir','app/src/main/res','-o',out/'resources.zip'])
manifest=(root/'app/src/main/AndroidManifest.xml').read_text().replace('<manifest ','<manifest package="pro.brickstorm.game" ',1)
(out/'AndroidManifest.xml').write_text(manifest)
run([bt/'aapt2','link','-o',out/'unsigned.apk','--manifest',out/'AndroidManifest.xml','-I',sdk,'--min-sdk-version','26','--target-sdk-version','35','--version-code','11','--version-name','1.8.0',out/'resources.zip'])
(out/'classes.list').write_text('\n'.join(str(p) for p in classes.rglob('*.class')))
run(['java','-cp',bt/'lib/d8.jar','com.android.tools.r8.D8','--lib',sdk,'--min-api','26','--output',out,'@'+str(out/'classes.list')])
with zipfile.ZipFile(out/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:z.write(out/'classes.dex','classes.dex')
run([bt/'zipalign','-p','-f','4',out/'unsigned.apk',out/'aligned.apk'])
args=['java','-jar',bt/'lib/apksigner.jar','sign','--debuggable-apk-permitted','false','--v4-signing-enabled','false']
old=os.environ.get('BRICKSTORM_PREVIOUS_KEYSTORE');lineage=os.environ.get('BRICKSTORM_LINEAGE')
if old and lineage:
 args+=['--ks',old,'--ks-key-alias',os.environ.get('BRICKSTORM_PREVIOUS_ALIAS','test'),'--ks-pass','env:BRICKSTORM_PREVIOUS_PASSWORD','--next-signer']
args+=['--ks',key,'--ks-key-alias',alias,'--ks-pass','env:BRICKSTORM_KEY_PASSWORD']
if old and lineage:args+=['--lineage',lineage,'--rotation-min-sdk-version','28']
apk=out/'BrickStorm-1.8.0.apk';args+=['--out',apk,out/'aligned.apk'];run(args)
run(['java','-jar',bt/'lib/apksigner.jar','verify','--verbose','--print-certs',apk])
run([bt/'aapt2','dump','badging',apk]);run([bt/'zipalign','-c','4',apk])
print('RELEASE_APK:',apk)
