package com.arcta.events;
class Dirs {
    BaseDirs baseDirs;final Util.FileContext fileContext;final String city;
    BaseDirs getBaseDirs() {
        return baseDirs;
    }
    Dirs(BaseDirs baseDirs, String city){this.baseDirs = baseDirs;this.fileContext = new Util.FileContext(null, null, null);this.city = city;}
    Dirs(BaseDirs baseDirs, Util.FileContext ctx, String city) {
        this.baseDirs = baseDirs;this.fileContext = ctx;this.city = city;}
    String getMergedDir() {String intermediateSubdir = Util.empty(fileContext.intermediate) ? "" : fileContext.intermediate + "/";
        return baseDirs.getBaseDir() + "intermediate" + "/" + intermediateSubdir + city + "/" + "merged" + "/";}
    String getUnmergedDir() {String intermediateSubdir = Util.empty(fileContext.intermediate) ? "" : fileContext.intermediate + "/";
        return baseDirs.getBaseDir() + "intermediate" + "/" + intermediateSubdir + city + "/" +"unmerged" + "/";}
    String getOutputDir() {String outputSubdir = Util.empty(fileContext.output) ? "" : fileContext.output + "/";
        return baseDirs.getBaseDir() + "output" + "/" + outputSubdir + city + "/";}
    String getHtmlDir() {String htmlSubdir = "html" + (Util.empty(fileContext.html) ? "" : "/" + fileContext.html) + (Util.empty(city) ? "" : "/" + city);
        return baseDirs.getBaseDir() + htmlSubdir + "/";}
    String getTsvDir() {String htmlSubdir = "tsv" + (Util.empty(fileContext.html) ? "" : "/" + fileContext.html) + (Util.empty(city) ? "" : "/" + city);
        return baseDirs.getBaseDir() + htmlSubdir + "/";}
    String getTsvMetaDir() {String htmlSubdir = "tsv/meta" + (Util.empty(fileContext.html) ? "" : "/" + fileContext.html) + (Util.empty(city) ? "" : "/" + city);
        return baseDirs.getBaseDir() + htmlSubdir + "/";}}