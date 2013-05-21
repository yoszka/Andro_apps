#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define USER_ID_STR_MAX  8		// up to 1000000 users :)
#define MSG_TAG_STR_MAX  255

#define FALSE  		 (0)
#define TRUE  		 (!FALSE)

void vPrintUsage(void);

typedef unsigned char BOOL;


BOOL bIsTagProvided 	= FALSE;
BOOL bisIdProvided 	= FALSE;
BOOL bVerboseModeEnabled = FALSE;



int main(int argc, char *argv[])
{
  char acUserId[USER_ID_STR_MAX] = {0};
  char acMsgTag[MSG_TAG_STR_MAX] = {0};  

  int i = 0;
  for(i = 0; i< argc; i++){
	if((strcmp (argv[i],"-v") == 0)){
		//printf("detect parameter \"%s\" \n", argv[i]);
		bVerboseModeEnabled = TRUE;
	}
	if((strcmp (argv[i],"-h") == 0) || (strcmp (argv[i],"-u") == 0)){				// -h print help/usage
		//printf("detect parameter \"%s\" \n", argv[i]);
		vPrintUsage();
	}
	if((strcmp (argv[i],"-id") == 0)){
		//printf("detect parameter \"%s\" \n", argv[i]);
		if(argc > (i+1)){
			i++;
			if((strlen(argv[i])) < USER_ID_STR_MAX){
				strcpy(acUserId, argv[i]);
				printf("User ID =  %s \n", acUserId);
				bisIdProvided = TRUE;
			}else{
				printf("ERROR: User ID is too long\n");
				exit(0);
			}
		}else{
			printf("ERROR: missing value for '-id' parameter\n");
			exit(0);
		}
	}
	if((strcmp (argv[i],"-t") == 0)){
		//printf("detect parameter \"%s\" \n", argv[i]);
		if(argc > (i+1)){
			i++;
			if((strlen(argv[i])) < MSG_TAG_STR_MAX){
				strcpy(acMsgTag, argv[i]);
				printf("message tag =  %s \n", acMsgTag);
				bIsTagProvided = TRUE;
			}else{
				printf("ERROR: Message tag is too long\n");
			}
		}else{
			printf("ERROR: missing value for '-t' parameter\n");
		}
	}
  }

  if(TRUE != bisIdProvided){
	printf("ERROR: Missing '-id' parameter, use '-h' for help\n");
	exit(0);
  }

  if(TRUE != bIsTagProvided){
	(FALSE != bVerboseModeEnabled)?printf("WARNING: custom tag not provided, use default, '-h' for help\n"):(void)0;
  }
//  printf("Hello World %i \"%s\" \n\n", argc, argv[0]);
//  system("api_key=AIzaSyA3DeMKrdtb6gG4kYdnbHbm-JUvm1ouikg; registration_id=APA91bGBQj9lhoOKrWIXUIo1gwrLCXz6zqf7RWaJmVQHg6ga8fhmiXHp2vTXOXPYHdEU-s_qBkiNHCP14Er-iewZ9ezXJHfyLt176wJFjciykKVMGwSFBqYrXAfNZIIeBQzEamYPmzIBZpLmysNmOjwrtXHzqVYtO3YzLB0VKBwg3eLN6i4oo48; token=event; message=some_text; curl -silent -output --header \"Authorization: key=$api_key\" --header Content-Type:\"application/json\" https://android.googleapis.com/gcm/send  -d \"{\\\"registration_ids\\\":[\\\"$registration_id\\\"],\\\"data\\\":{\\\"token\\\":\\\"$token\\\",\\\"message\\\":\\\"$message\\\"}}\"");
  return 0;
}

void vPrintUsage(void){
printf("Usage:\n"
"-h\t - print this help\n"
"-u\t - same as '-h'\n"
"-v\t - verbose mode\n"
"-id n\t - where 'n' is user ID to send notification <obligatory parameter>\n"
"-t  s\t - where 's' is message tag, when isn't set then default 'event' is provided <optional parameter>\n");
}
