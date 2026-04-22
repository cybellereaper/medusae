module Medusae
  module Http
    class DiscordHttpError < Exception
      getter status_code : Int32
      getter body : String

      def initialize(@status_code : Int32, @body : String)
        super("Discord API request failed with status=#{status_code}")
      end
    end

    class TransportError < Exception
    end
  end
end
