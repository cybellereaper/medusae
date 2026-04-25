module Medusae
  module Gateway
    enum GatewayIntent
      Guilds
      GuildMessages
      DirectMessages
      MessageContent

      def bit : Int32
        case self
        when Guilds
          1
        when GuildMessages
          1 << 9
        when DirectMessages
          1 << 12
        when MessageContent
          1 << 15
        else
          raise "unreachable"
        end
      end

      def self.combine(intents : Enumerable(GatewayIntent)) : Int32
        intents.reduce(0) { |acc, intent| acc | intent.bit }
      end

      def self.combine(*intents : GatewayIntent) : Int32
        combine(intents)
      end
    end
  end
end
