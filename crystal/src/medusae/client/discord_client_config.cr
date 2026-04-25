module Medusae
  module Client
    class DiscordClientConfig
      getter bot_token : String
      getter intents : Int32
      getter api_base_url : String
      getter api_version : Int32
      getter request_timeout : Time::Span
      getter shard_id : Int32
      getter shard_count : Int32

      def initialize(
        @bot_token : String,
        @intents : Int32 = 0,
        api_base_url : String? = nil,
        api_version : Int32 = 10,
        request_timeout : Time::Span? = nil,
        @shard_id : Int32 = 0,
        @shard_count : Int32 = 1
      )
        raise ArgumentError.new("bot_token must not be blank") if @bot_token.strip.empty?

        @api_base_url = normalize_base_url(api_base_url)
        @api_version = api_version > 0 ? api_version : 10
        @request_timeout = request_timeout || 30.seconds

        validate_sharding!
      end

      def api_uri(path : String) : String
        normalized_path = path.starts_with?("/") ? path : "/#{path}"
        "#{@api_base_url}/v#{@api_version}#{normalized_path}"
      end

      private def normalize_base_url(url : String?) : String
        value = url.to_s.strip
        value = "https://discord.com/api" if value.empty?
        value.gsub(/\/+$/, "")
      end

      private def validate_sharding! : Nil
        raise ArgumentError.new("shard_count must be >= 1") if @shard_count < 1

        valid_shard_id = @shard_id >= 0 && @shard_id < @shard_count
        return if valid_shard_id

        raise ArgumentError.new("shard_id must be between 0 (inclusive) and shard_count (exclusive)")
      end
    end
  end
end
