#!/usr/bin/python

# We expect comments to be of the form GF0000 : <comment text>
#
# The length of <comment text> can not be zero or all blanks, but it
# can span multiple lines.
#
# The first parameter to the script must be the comment file.
# The second (optional) parameter is the minimum comment length
# The third (optional and as yet unused) should be the module, to 
# allow this script to be used for different projects

import sys

def main(args):

	MIN_COMMENT_LENGTH = 0
		
	if len(args) > 1:
		try:
			MIN_COMMENT_LENGTH = int(args[1])
		except:
			pass
	
	log = open(args[0], "r");	
	log_comment = ' '.join(log)
	log_comment = log_comment.splitlines()
	log_comment = ' '.join(log_comment)	
	log_comment = log_comment.strip()
	
	log.close()
		
	""" comment must begin with 'GF' (case insensitive) """
	if not log_comment.upper().startswith('GF'):
#		print "log comment does not start with 'GF'"
		return 1
		
	contents = log_comment.split(':', 1)
	
	""" comment line must include a colon and non-zero length comment after the colon """
	""" note that it would also be easy enough to allow them to skip the colon if the 
	    required minimum length is 0 """
	if len(contents) < 2:
#		print "log comment does not include colon"
		return 1
		
	""" get the numeric part of the GF tracker identifier (everything except the leading 'GF') """
	gf_number = contents[0][2:].strip()
	
	""" reconstruct the comment text as a single string, with all blanks removed """
	comment_text = contents[1].replace(" ", "")	
#	print comment_text
	
	if not gf_number.isdigit():
#		print "GF tracker number not numeric: [%s]" % gf_number
		return 1

	if not len(comment_text.strip()) >= MIN_COMMENT_LENGTH:	
#		print "comment text not greater than pre-defined minimum"
		return 1
	
	return 0

if __name__ == '__main__':	
	sys.exit(main(sys.argv[1:]))
#	retval = main(sys.argv[1:])
#	print retval
#	sys.exit(retval)
		