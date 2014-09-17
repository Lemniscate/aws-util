# AWS Utils

Just a simple collection of utilities for working with AWS.

Right now, it mainly consists of S3 related functionality.

### StorageDestination
Contains an abstraction for pre-signing upload URLs to S3.

A simple example:

```
Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.MONTH, 1);
    Date expiration = cal.getTime();

    StorageDestination destination = new StorageDestination.Builder()
            .setKey("files/foobar/test.jpg")
            .policyBuilder(expiration)
                .setBucket("supertemporarybucket")
                .setKeyPrefix("files/foobar")
                .setAcl("public-read")
                .setContentType("image/")
                .setRedirect("http://google.com?accessCode=5")
                .done(ACCESS_KEY, SECRET_KEY)
            .build()
            ;
```

This information can be used to upload the file via S3's REST API. The serialized payload
would look something like:

```
{
  "url" : "https://supertemporarybucket.s3.amazonaws.com",
  "method" : "POST",
  "expiration" : 1413567648714,
  "params" : {
    "success_action_redirect" : "http://google.com?accessCode=5",
    "acl" : "public-read",
    "policy" : "eyJleHBpcmF0aW9uIjoiMjAxNC0xMC0xN1QxNzo0MDo0OFoiLCJjb25kaXRpb25zIjpbeyJidWNrZXQiOiJzdXBlcnRlbXBvcmFyeWJ1Y2tldCJ9LFsic3RhcnRzLXdpdGgiLCIka2V5IiwiZmlsZXMvZm9vYmFyIl0seyJhY2wiOiJwdWJsaWMtcmVhZCJ9LHsic3VjY2Vzc19hY3Rpb25fcmVkaXJlY3QiOiJodHRwOi8vZ29vZ2xlLmNvbT9hY2Nlc3NDb2RlPTUifV19",
    "AWSAccessKeyId" : "AKIAIAFHVJDIP427B4LA",
    "Content-Type" : "image/",
    "signature" : "SH7pV+rrG+7gKRUxkJwd3hUWWgZ=",
    "key" : "files/foobar/test.jpg"
  }
}
```

which you would use to build your multipart form submission (including the file as *file* )