variable "aws_region" {
  type        = string
  description = "Region where the AWS servers will live"
  default     = "us-east-1"
}

variable "civiform_time_zone_id" {
  type        = string
  description = "Time zone for Civiform server to use when displaying dates."
  default     = "America/Los_Angeles"
}

variable "civic_entity_short_name" {
  type        = string
  description = "Short name for civic entity (example: Rochester, Seattle)."
  default     = "Dev Civiform"
}

variable "civic_entity_full_name" {
  type        = string
  description = "Full name for civic entity (example: City of Rochester, City of Seattle)."
  default     = "City of Dev Civiform"
}

variable "civic_entity_support_email_address" {
  type        = string
  description = "Email address where applicants can contact civic entity for support with Civiform."
  default     = "azizoval@google.com"
}

variable "civic_entity_logo_with_name_url" {
  type        = string
  description = "Logo with name used on the applicant-facing program index page"
  default     = "https://raw.githubusercontent.com/civiform/staging-azure-deploy/main/logos/civiform-staging-long.png"
}

variable "civic_entity_small_logo_url" {
  type        = string
  description = "Logo with name used on the applicant-facing program index page"
  default     = "https://raw.githubusercontent.com/civiform/staging-azure-deploy/main/logos/civiform-staging.png"
}

variable "docker_username" {
  type        = string
  description = "Docker username"
  default     = "civiform"
}

variable "docker_repository_name" {
  type        = string
  description = "Name of container image"
  default     = "civiform"
}

variable "location_name" {
  type        = string
  description = "Name of the location for the resource group"
  default     = "eastus"
}

variable "vnet_address_space" {
  type        = list(string)
  description = "List of vnet address_spaces"
  default = [
    "10.0.0.0/16"
  ]
}

variable "subnet_address_prefixes" {
  type        = list(string)
  description = "List of the apps subnet address prefixes (must be distinct from the postgress subnet)"
  default = [
    "10.0.2.0/24"
  ]
}

variable "canary_subnet_address_prefixes" {
  type        = list(string)
  description = "List of the apps subnet address prefixes (must be distinct from the postgress subnet)"
  default = [
    "10.0.0.0/24"
  ]
}

variable "bastion_address_prefixes" {
  type        = list(string)
  description = "Prefixes for the bastion instance (must be distinct from other subnets)"
  default = [
    "10.0.6.0/24"
  ]
}

variable "app_sku" {
  type        = map(string)
  description = "SKU tier/size/capacity information"
  default = {
    tier     = "Standard",
    size     = "S2",
    capacity = "2"
  }
}

variable "postgres_sku_name" {
  type        = string
  description = "The sku name for postgres server"
  default     = "GP_Gen5_2"
}

variable "postgres_storage_mb" {
  type        = number
  description = "The mb of storage for postgres instance"
  default     = 5120
}

variable "postgres_backup_retention_days" {
  type        = number
  description = "Number of days to retain postgres backup"
  default     = 7
}

variable "postgres_subnet_address_prefixes" {
  type        = list(string)
  description = "A list of the subnet address prefixes for postgres (must be distinct from the app's subnet addresses)"
  default = [
    "10.0.4.0/24"
  ]
}

variable "log_sku" {
  type        = string
  description = "The SKU for the sever logs"
  default     = "PerGB2018"
}

variable "log_retention" {
  type        = number
  description = "The number of days the logs will be retained for"
  default     = 30
}

variable "ses_sender_email" {
  type        = string
  description = "Email address of who is sending the email, passed to the app"
  default     = "azizoval@google.com"
}

variable "adfs_admin_group" {
  type        = string
  description = "Active Directory Federation Service group name"
  default     = "ec42e57f-0fff-48ec-a345-908eed538668"
}

variable "ad_groups_attribute_name" {
  type        = string
  description = "Name of the Active Directory claim that returns groups a user is in"
  default     = "groups"
}

variable "civiform_applicant_idp" {
  type        = string
  description = "identity provider to use for applicant auth. supported values are idcs and login-radius"
  default     = "login-radius"
}

variable "login_radius_api_key" {
  type        = string
  description = "Login Radius API Key"
  default     = "CHANGE_ME"
}

variable "login_radius_metadata_uri" {
  type        = string
  description = "LoginRadius endpoint for fetching IdP metadata"
  default     = "https://civiform-staging.hub.loginradius.com/service/saml/idp/metadata"
}

variable "login_radius_saml_app_name" {
  type        = string
  description = "The App Name for the LoginRadius SAML integration"
  default     = "civiform-saml"
}

variable "saml_keystore_filename" {
  description = "Name of Java Keystore file stored in Azure blob storage"
  default     = null
}

variable "saml_keystore_password" {
  description = "Password for Java Keystore file"
  default     = null
}

variable "saml_private_key_password" {
  description = "Password for Java Keystore private key"
  default     = null
}

variable "saml_keystore_storage_account_name" {
  description = "Name of storage account where Java Keystore is stored"
  default     = null
}

variable "saml_keystore_storage_container_name" {
  description = "Name of storage container where Java Keystore is stored"
  default     = null
}

variable "saml_keystore_storage_access_key" {
  description = "Key needed to access keystore file"
  default     = null
}

variable "staging_program_admin_notification_mailing_list" {
  type        = string
  description = "Admin notification mailing list for staging"
  default     = ""
}

variable "staging_ti_notification_mailing_list" {
  type        = string
  description = "intermediary notification mailing list for staging"
  default     = ""
}

variable "staging_applicant_notification_mailing_list" {
  type        = string
  description = "Applicant notification mailing list for staging"
  default     = ""
}