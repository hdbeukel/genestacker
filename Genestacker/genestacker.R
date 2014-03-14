############################
# Gene Stacker R interface #
############################

# procedure to run Gene Stacker
genestacker.run <- function(input,          # input file path
                            output,         # output file path
                            g,              # maximum number of generations (required)
                            s,              # overall success rate (required)
                            tc=NA,          # maximum total number of crossings
                            S=NA,           # number of seeds produced by one crossing
                            lpa=NA,         # maximum linkage phase ambiguity (overall)
                            p=NA,           # maximum plants per generation
                            hip=FALSE,      # homozygous ideotype parents
                            c=NA,           # maximum crossings with one plant
                            gf=NA,          # graphics file format
                            k=FALSE,        # kosambi mapping function
                            tree=FALSE,     # construct schedules with tree structure (no reuse)
                            minp=FALSE,     # minimize the population size only
                            rt=NA,          # runtime limit
                            thr=NA,         # number of threads used to extend partial schedule
                            best=FALSE,     # best preset
                            better=FALSE,   # better preset
                            faster=FALSE,   # faster preset
                            fastest=FALSE,  # fastest preset
                            mco=NA,         # maximum number of crossovers per chromosome
                            h0=FALSE,       # heuristic H0
                            h1a=FALSE,      # heuristic H1a
                            h1b=FALSE,      # heuristic H1b
                            h2a=FALSE,      # heuristic H2a
                            h2b=FALSE,      # heuristic H2b
                            h3=FALSE,       # heuristic H3
                            h3s1=FALSE,     # heuristic H3s1
                            h3s2=FALSE,     # heuristic H3s2
                            h4=FALSE,       # heuristic H4
                            h5=FALSE,       # heuristic H5
                            h5c=FALSE,      # heuristic H5c
                            h6=FALSE,       # heuristic H5c
                            v=FALSE,        # be extra verbose
                            vv=FALSE,       # be ridiculously verbose (overrides v)
                            int=FALSE,      # create intermediate output file whenever Pareto frontier has changed
                            mem="2g")       # reserved RAM memory (defaults to 2 GB)
                            {
        
    # setup options
    
    # required
    options = paste("-g", g)
    options = paste("-s", s, options)
    
    # optional
    if(!(is.na(tc))){
        options = paste("-tc", tc, options)
    }
    if(!(is.na(S))){
        options = paste("-S", S, options)
    }
    if(!(is.na(lpa))){
        options = paste("-lpa", lpa, options)
    }
    if(!(is.na(p))){
        options = paste("-p", p, options)
    }
    if(hip){
        options = paste("-hip", options)
    }
    if(!(is.na(c))){
        options = paste("-c", c, options)
    }
    if(!(is.na(gf))){
        options = paste("-gf", gf, options)
    }
    if(k){
        options = paste("-k", options)
    }
    if(tree){
        options = paste("-tree", options)
    }
    if(minp){
        options = paste("-minp", options)
    }
    if(!(is.na(rt))){
        options = paste("-rt", rt, options)
    }
    if(!(is.na(thr))){
        options = paste("-thr", thr, options)
    }
    if(best){
        options = paste("-best", options)
    }
    if(better){
        options = paste("-better", options)
    }
    if(faster){
        options = paste("-faster", options)
    }
    if(fastest){
        options = paste("-fastest", options)
    }
    if(!(is.na(mco))){
        options = paste("-mco", mco, options)
    }
    if(h0){
        options = paste("-h0", options)
    }
    if(h1a){
        options = paste("-h1a", options)
    }
    if(h1b){
        options = paste("-h1b", options)
    }
    if(h2a){
        options = paste("-h2a", options)
    }
    if(h2b){
        options = paste("-h2b", options)
    }
    if(h3){
        options = paste("-h3", options)
    }
    if(h3s1){
        options = paste("-h3s1", options)
    }
    if(h3s2){
        options = paste("-h3s2", options)
    }
    if(h4){
        options = paste("-h4", options)
    }
    if(h5){
        options = paste("-h5", options)
    }
    if(h5c){
        options = paste("-h5c", options)
    }
    if(h6){
        options = paste("-h6", options)
    }
    if(v){
        options = paste("-v", options)
    }
    if(vv){
        options = paste("-vv", options)
    }
    if(int){
        options = paste("-int", options)
    }
	
    # run CLI
    mem = paste("-Xmx", mem, sep="")
    system(paste("java", mem, "-jar", genestacker.jar(), options, input, output))
    
}

# get Gene Stacker version
genestacker.version <- function(){
    system(paste("java -jar", genestacker.jar(), "-version"))
}

# get path to jar file
genestacker.jar <- function(){
    # get path to Gene Stacker CLI jar file
    cli = "genestacker.jar"
    # try bin folder if not found in current directory
    if(!file.exists(cli)){
      cli = "bin/genestacker.jar"
    }
    # if CLI not found, terminate with error message
    if(!file.exists(cli)){
      stop("Gene Stacker CLI jar file not found.")
    }
    # return path
    return(cli)
} 



