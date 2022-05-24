# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/apprunner_service

resource "aws_apprunner_service" "civiform_dev" {
  auto_scaling_configuration_arn = aws_apprunner_auto_scaling_configuration_version.auto-scaling-config.arn
  service_name                   = "civiform_dev"

  source_configuration {
    image_repository {
      image_configuration {
        port = "9000"

        runtime_environment_variables = {
          SECRET_KEY = "secretkeyplaceholder"
          PORT       = "9000"

          DB_JDBC_STRING = "jdbc:postgresql://${aws_db_instance.civiform.address}:${aws_db_instance.civiform.port}/postgres?ssl=true&sslmode=require"
          DB_USERNAME    = aws_db_instance.civiform.username
          DB_PASSWORD    = aws_db_instance.civiform.password

          STAGING_HOSTNAME = "staging-aws.civiform.dev"
          BASE_URL         = "https://staging-aws.civiform.dev"

          STORAGE_SERVICE_NAME = "s3"
          AWS_S3_BUCKET_NAME   = "${aws_s3_bucket.b.id}"

          CIVIFORM_TIME_ZONE_ID              = var.civiform_time_zone_id
          WHITELABEL_CIVIC_ENTITY_SHORT_NAME = var.civic_entity_short_name
          WHITELABEL_CIVIC_ENTITY_FULL_NAME  = var.civic_entity_full_name
          WHITELABEL_SMALL_LOGO_URL          = var.civic_entity_small_logo_url
          WHITELABEL_LOGO_WITH_NAME_URL      = var.civic_entity_logo_with_name_url
          SUPPORT_EMAIL_ADDRESS              = var.civic_entity_support_email_address

          AWS_SES_SENDER        = var.ses_sender_email
          AWS_ACCESS_KEY_ID     = "CHANGE_ME"
          AWS_SECRET_ACCESS_KEY = "CHANGE_ME"
          AWS_REGION            = var.aws_region

          STAGING_ADMIN_LIST     = var.staging_program_admin_notification_mailing_list
          STAGING_TI_LIST        = var.staging_ti_notification_mailing_list
          STAGING_APPLICANT_LIST = var.staging_applicant_notification_mailing_list

          AD_GROUPS_ATTRIBUTE_NAME = var.ad_groups_attribute_name
          ADFS_SECRET              = ""
          ADFS_CLIENT_ID           = ""
          ADFS_DISCOVERY_URI       = ""
          ADFS_GLOBAL_ADMIN_GROUP  = var.adfs_admin_group
          CIVIFORM_APPLICANT_IDP   = var.civiform_applicant_idp

          ADFS_ADDITIONAL_SCOPES = ""
        }
      }

      image_identifier      = "public.ecr.aws/t1q6b4h2/universal-application-tool:prod"
      image_repository_type = "ECR_PUBLIC"
    }

    auto_deployments_enabled = false
  }
  depends_on = [aws_iam_role.apprunner-service-role]
}

resource "aws_db_parameter_group" "civiform" {
  name   = "civiform"
  family = "postgres11"

  parameter {
    name  = "log_connections"
    value = "1"
  }
}

resource "aws_db_instance" "civiform" {
  identifier             = "civiform"
  instance_class         = "db.t3.micro"
  allocated_storage      = 5
  engine                 = "postgres"
  engine_version         = "11"
  username               = "db_user"
  password               = "CHANGE_ME"
  db_subnet_group_name   = aws_db_subnet_group.civiform.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  parameter_group_name   = aws_db_parameter_group.civiform.name
  publicly_accessible    = true
  skip_final_snapshot    = true
}

resource "aws_s3_bucket" "b" {
  bucket = "civiform-files-s3"
}

resource "aws_s3_bucket_acl" "example" {
  bucket = aws_s3_bucket.b.id
  acl    = "private"
}

resource "aws_s3_bucket_public_access_block" "app" {
  bucket                  = aws_s3_bucket.b.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
